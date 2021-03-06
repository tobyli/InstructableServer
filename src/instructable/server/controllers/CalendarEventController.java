package instructable.server.controllers;

import instructable.server.backend.ExecutionStatus;
import instructable.server.hirarchy.CalendarEvent;
import instructable.server.hirarchy.ConceptContainer;
import instructable.server.hirarchy.GenericInstance;
import instructable.server.hirarchy.InstanceContainer;
import instructable.server.senseffect.ICalendarAccessor;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Amos Azaria on 14-Mar-16.
 */
public class CalendarEventController
{
    InstanceContainer instanceContainer;
    Optional<ICalendarAccessor> calendarAccessor;
    private static final String draftPrefix = "draft";
    private static final String savedPrefix = "saved";
    long numOfDrafts; //retrieved from the DB
    long numOfEventsSaved;

    Date lastRequestedEventStartTime = new Date();
    Double lastRequestedEventDuration = 0.0;

    public CalendarEventController(ConceptContainer conceptContainer, InstanceContainer instanceContainer, Optional<ICalendarAccessor> calendarAccessor)
    {
        this.calendarAccessor = calendarAccessor;
        this.instanceContainer = instanceContainer;
        conceptContainer.defineConcept(new ExecutionStatus(), CalendarEvent.strCalendarEventTypeAndName, CalendarEvent.getFieldDescriptions(true));
        List<GenericInstance> allOutEmails = instanceContainer.getAllInstances(CalendarEvent.strCalendarEventTypeAndName);
        numOfDrafts = allOutEmails.stream().filter(x->x.getName().startsWith(draftPrefix)).count();
        numOfEventsSaved = allOutEmails.stream().filter(x->x.getName().startsWith(savedPrefix)).count();
    }

    public Optional<GenericInstance> getNextPrevCalendarEvent(ExecutionStatus executionStatus, boolean next)
    {
        if (!calendarAccessor.isPresent())
        {
            executionStatus.add(ExecutionStatus.RetStatus.error, "no calendar is attached");
            return Optional.empty();
        }
        Optional<ICalendarAccessor.EventFields> event;
        event = calendarAccessor.get().getNextPrevCalendarEvent(next, lastRequestedEventStartTime, lastRequestedEventDuration);
        if (!event.isPresent())
        {
            executionStatus.add(ExecutionStatus.RetStatus.error, "no " + ( next ? "next":"previous") + "event was found.");
            return Optional.empty();
        }
        lastRequestedEventStartTime = event.get().time;
        lastRequestedEventDuration =event.get().durationInMinutes;
        Optional<GenericInstance> instance = instanceContainer.getInstance(new ExecutionStatus(), CalendarEvent.strCalendarEventTypeAndName, event.get().eventId);
        if (instance.isPresent())
            return Optional.of(instance.get());
        new CalendarEvent(instanceContainer, event.get(), true);
        instance = instanceContainer.getInstance(new ExecutionStatus(), CalendarEvent.strCalendarEventTypeAndName, event.get().eventId);
        //should have it now
        if (instance.isPresent())
            return Optional.of(instance.get());
        executionStatus.add(ExecutionStatus.RetStatus.error, "couldn't update database.");
        return Optional.empty();
    }

    public void saveEvent(ExecutionStatus executionStatus)
    {
        if (!calendarAccessor.isPresent())
        {
            executionStatus.add(ExecutionStatus.RetStatus.error, "no calendar is attached.");
        }
        else
        {
            Optional<CalendarEvent>  composedCalendarEvent = getCalendarEventBeingComposed(executionStatus);
            if (composedCalendarEvent.isPresent() && checkSavingPrerequisites(executionStatus, composedCalendarEvent.get()))
            {
                CalendarEvent eventInfo = composedCalendarEvent.get();
                calendarAccessor.get().SaveEvent(new ICalendarAccessor.EventFields(
                        "", //no event id yet
                        eventInfo.getTitle(),
                        eventInfo.getDescription(),
                        eventInfo.getDate().get(), //must have date, because checked in prerequisites.
                        eventInfo.getDuration(),
                        eventInfo.getParticipants()
                ));
                if (executionStatus.noError())
                {
                    instanceContainer.setMutability(executionStatus, CalendarEvent.strCalendarEventTypeAndName, CalendarEvent.strCalendarEventTypeAndName, false);
                    instanceContainer.renameInstance(executionStatus, CalendarEvent.strCalendarEventTypeAndName, CalendarEvent.strCalendarEventTypeAndName, getCurrentSavedName());
                    numOfEventsSaved++;
                }
            }
        }
    }

    private boolean checkSavingPrerequisites(ExecutionStatus executionStatus, CalendarEvent calendarEvent)
    {
        if (calendarEvent.hasDate())
            return true;
        else
        executionStatus.add(ExecutionStatus.RetStatus.error, "the event has no time associated with it. Please set the date and time before saving it to your calendar");
        return false;
    }

    public Optional<CalendarEvent> getCalendarEventBeingComposed(ExecutionStatus executionStatus)
    {
        Optional<GenericInstance> composedCalendarEvent = instanceContainer.getInstance(executionStatus, CalendarEvent.strCalendarEventTypeAndName, CalendarEvent.strCalendarEventTypeAndName);
        if (composedCalendarEvent.isPresent())
        {
            return Optional.of(new CalendarEvent(composedCalendarEvent.get()));
        }
        else
        {
            executionStatus.add(ExecutionStatus.RetStatus.error, "there is no event being composed");
            return Optional.empty();
        }
    }

    public void createNewEvent(ExecutionStatus executionStatus)
    {
        //first rename old one to draft if exists.
        Optional<GenericInstance> currentCalendarEvent = instanceContainer.getInstance(new ExecutionStatus(), CalendarEvent.strCalendarEventTypeAndName, CalendarEvent.strCalendarEventTypeAndName);
        if (currentCalendarEvent.isPresent())
        {
            instanceContainer.setMutability(executionStatus, CalendarEvent.strCalendarEventTypeAndName, CalendarEvent.strCalendarEventTypeAndName, false); //old draft will be immutable, user will need to restore draft in order to change it
            instanceContainer.renameInstance(new ExecutionStatus(), currentCalendarEvent.get().getConceptName(), currentCalendarEvent.get().getName(), getCurrentDraftName());
            numOfDrafts++;
            //instanceContainer.deleteInstance(new ExecutionStatus(), emailBeingComposed.get());
        }
        //now create a new one
        new CalendarEvent(instanceContainer, CalendarEvent.strCalendarEventTypeAndName, true);
    }

    public void restoreEventFrom(ExecutionStatus executionStatus, boolean restoreFromDraft)
    {
        if (restoreFromDraft)
        {
            if (numOfDrafts > 0)
            {
                deleteCurrentEventIfExists();
                numOfDrafts--;
                instanceContainer.renameInstance(executionStatus, CalendarEvent.strCalendarEventTypeAndName, getCurrentDraftName(), CalendarEvent.strCalendarEventTypeAndName);
                if (executionStatus.isOkOrComment())
                    instanceContainer.setMutability(new ExecutionStatus(), CalendarEvent.strCalendarEventTypeAndName, CalendarEvent.strCalendarEventTypeAndName, true);
            }
            else
                executionStatus.add(ExecutionStatus.RetStatus.error, "no draft found");
        }
        else //restoreFromSaved
        {
            if (numOfEventsSaved > 0)
            {
                deleteCurrentEventIfExists();
                numOfEventsSaved--;
                instanceContainer.renameInstance(executionStatus, CalendarEvent.strCalendarEventTypeAndName, getCurrentSavedName(), CalendarEvent.strCalendarEventTypeAndName);
                if (executionStatus.isOkOrComment())
                    instanceContainer.setMutability(new ExecutionStatus(), CalendarEvent.strCalendarEventTypeAndName, CalendarEvent.strCalendarEventTypeAndName, true);
            }
            else
                executionStatus.add(ExecutionStatus.RetStatus.error, "no saved event found");
        }
    }

    private void deleteCurrentEventIfExists()
    {
        Optional<GenericInstance> eventBeingComposed = instanceContainer.getInstance(new ExecutionStatus(), CalendarEvent.strCalendarEventTypeAndName, CalendarEvent.strCalendarEventTypeAndName);
        if (eventBeingComposed.isPresent())
        {
            instanceContainer.deleteInstance(new ExecutionStatus(), eventBeingComposed.get());
        }
    }

    private String getCurrentDraftName()
    {
        return draftPrefix + numOfDrafts;
    }

    private String getCurrentSavedName()
    {
        return savedPrefix + numOfEventsSaved;
    }


}
