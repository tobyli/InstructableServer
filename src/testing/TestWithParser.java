package testing;

import instructable.server.backend.IAllUserActions;
import instructable.server.backend.TopDMAllActions;
import instructable.server.ccg.CcgUtils;
import instructable.server.ccg.ParserSettings;
import instructable.server.dal.CreateParserFromFiles;
import instructable.server.dal.DBUtils;
import instructable.server.hirarchy.EmailInfo;
import instructable.server.parser.CommandsToParser;
import instructable.server.senseffect.IIncomingEmailControlling;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Created by Amos Azaria on 20-Apr-15.
 */
public class TestWithParser
{
    static boolean testingMode = true;
    static String fileName = "Aug04test.txt";
    //static final int waitForLearning = 4000;//not required any more

    public static void main(String[] args) throws Exception
    {
        runTest();
    }

    private static void runTest() throws Exception
    {
        String userId = "tempUser";

        DBUtils.clearUserData(userId);

        TestHelpers testHelpers = new TestHelpers(testingMode, fileName);

        ParserSettings parserSettings = CreateParserFromFiles.createParser(Optional.of(userId));

        IAllUserActions allUserActions = new TopDMAllActions("you@myworkplace.com", userId, new CommandsToParser(parserSettings), (subject, body, copyList, recipientList) -> {}, false, Optional.empty(), Optional.empty());

        TestSimplifier testSimplifier = new TestSimplifier(allUserActions, testHelpers, parserSettings);


        sendingBasicEmail(allUserActions, testHelpers, parserSettings);

        definingContact(allUserActions, testHelpers, parserSettings);

        setFromGet(allUserActions, testHelpers, parserSettings);

        teachingToSetRecipientAsContact(allUserActions, testHelpers, parserSettings);

        buildRequiredDB((TopDMAllActions) allUserActions, testHelpers, parserSettings);

        learningToForwardAnEmail(allUserActions, testHelpers, parserSettings);

        smallUpdates(allUserActions, testHelpers, parserSettings);

        testSimplifier.newSection("replyAndRead");
        testSimplifier.userSays(new String[]{"reply blue", "yes", "compose email", "set body to blue", "end"});
        //Thread.sleep(waitForLearning);
        testSimplifier.userSays("reply I'm still at work where are you");
        testSimplifier.userSays(new String[]{"previous email", "read email"});
        testSimplifier.userSays(new String[]{"new command", "next email plus reading it", "next email", "read email", "end", "move to the previous email in the inbox"});
        //Thread.sleep(waitForLearning);
        testSimplifier.userSays("next email plus reading it");
        testSimplifier.userSays("read email");

        testSimplifier.userSays(new String[] {"learn a new command","cancel"});
        testSimplifier.userSays(new String[] {"new command","make bob the recipient","cancel"});

        testSimplifier.userSays(new String[] {"create new email", "set subject to hello", "set subject to good-bye", "create new email", "undo", "read outgoing email", "undo last command"});

        testHelpers.endTest();
        //emailSomeoneSomeText()
    }

    private static void sendingBasicEmail(IAllUserActions allUserActions, TestHelpers testHelpers, ParserSettings parserSettings)
    {
        CcgUtils.SayAndExpression response;
        String userSays;

        userSays = "send an email";
        testHelpers.userSays(userSays);
        response = parserSettings.parseAndEval(allUserActions, userSays);
        //actionResponse = allUserActions.sendEmail(new InfoForCommand(userSays,null));
        testHelpers.systemSays(response.sayToUser);

        userSays = "yes";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.yes(new InfoForCommand(userSays,null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "set the subject of this email to hello";
        // (setFieldFromString (getProbFieldByInstanceNameAndFieldName outgoing_email subject) "hello")
        testHelpers.userSays(userSays);
        response = parserSettings.parseAndEval(allUserActions, userSays);
        //actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "outgoing email", "subject");
        //if (response.isSuccess())
        //actionResponse = allUserActions.setFieldFromString(new InfoForCommand(userSays, null), response.getField(), "hello");
        testHelpers.systemSays(response.sayToUser);


        //System.exit(0);

        userSays = "put test in body";
        testHelpers.userSays(userSays);
        response = parserSettings.parseAndEval(allUserActions, userSays);
//        actionResponse = allUserActions.getProbFieldByFieldName(new InfoForCommand(userSays, null), "body");
//        if (response.isSuccess())
//        {
//            actionResponse = allUserActions.setFieldFromString(new InfoForCommand(userSays, null), response.getField(), "test");
//        }
        testHelpers.systemSays(response.sayToUser);

        userSays = "send the email";
        testHelpers.userSays(userSays);
        response = parserSettings.parseAndEval(allUserActions, userSays);
        //actionResponse = allUserActions.sendEmail(new InfoForCommand(userSays,null));
        testHelpers.systemSays(response.sayToUser);

        userSays = "set myself as the recipient";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByFieldName(new InfoForCommand(userSays, null), "recipient list");
//        if (response.isSuccess())
//        {
//            actionResponse = allUserActions.setFieldFromString(new InfoForCommand(userSays, null), response.getField(), "myself");
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        //how should we know that recipient is recipient list? Leave it for the parser?
        testHelpers.systemSays(response.sayToUser);

        userSays = "set myself@myjob.com as the recipient";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByFieldName(new InfoForCommand(userSays, null), "recipient list");
//        if (response.isSuccess())
//        {
//            actionResponse = allUserActions.setFieldFromString(new InfoForCommand(userSays, null), response.getField(), "myself@myjob.com");
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        //could learn something from this?!
        testHelpers.systemSays(response.sayToUser);

        userSays = "send";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.sendEmail(new InfoForCommand(userSays,null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "send";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.sendEmail(new InfoForCommand(userSays,null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);
    }

    private static void definingContact(IAllUserActions allUserActions, TestHelpers testHelpers, ParserSettings parserSettings)
    {
        testHelpers.newSection("definingContact");

        CcgUtils.SayAndExpression response;
        //ActionResponse actionResponse;
        String userSays;

        userSays = "compose a new email";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.createInstanceByConceptName(new InfoForCommand(userSays, null), "outgoing email");
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "set danny as the recipient";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByFieldName(new InfoForCommand(userSays, null), "recipient list");
//        if (response.isSuccess())
//        {
//            actionResponse = allUserActions.setFieldFromString(new InfoForCommand(userSays, null), response.getField(), "my spouse");
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "define contact.";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.defineConcept(new InfoForCommand(userSays, null), "contact");
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "Define contact!";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.defineConcept(new InfoForCommand(userSays,null), "contact");
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "add email as a field in contact";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.addFieldToConcept(new InfoForCommand(userSays,null), "contact", "email");
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "create a contact call it bob";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.createInstanceByFullNames(new InfoForCommand(userSays, null), "contact", "bob");
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "set bob's email to baba";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "bob", "email");
//        if (response.isSuccess())
//        {
//            actionResponse = allUserActions.setFieldFromString(new InfoForCommand(userSays, null), response.getField(), "baba");
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "set bob's email to bob@gmail.com";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "bob", "email");
//        if (response.isSuccess())
//        {
//            actionResponse = allUserActions.setFieldFromString(new InfoForCommand(userSays, null), response.getField(), "bob@gmail.com");
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

    }


    private static void setFromGet(IAllUserActions allUserActions, TestHelpers testHelpers, ParserSettings parserSettings)
    {
        testHelpers.newSection("setFromGet");

        //ActionResponse actionResponse;
        CcgUtils.SayAndExpression response;
        String userSays;

        userSays = "what is bob's email?";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "bob", "email");
//        if (response.isSuccess())
//        {
//            //the parser should no not to return a field to the user but first evaluate it
//            actionResponse = allUserActions.evalField(new InfoForCommand(userSays, null), response.getField());
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);
        //testHelpers.systemSays(response.value.get().toJSONString());

        userSays = "create a contact jane";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.createInstanceByFullNames(new InfoForCommand(userSays, null), "contact", "jane");
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);


        userSays = "take bob's email";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "bob", "email");
//        if (response.isSuccess())
//        {
//            //maybe this should be done automatically every time.
//            actionResponse = allUserActions.evalField(new InfoForCommand(userSays, null), response.getField());
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "and set it as jane's email";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "jane", "email");
//        if (response.isSuccess())
//        {
//            actionResponse = allUserActions.setFieldFromPreviousEval(new InfoForCommand(userSays, null), response.getField());
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);


        userSays = "take bob's email and set it as jane's email";
        testHelpers.userSays(userSays);
        //parser should translate to:
        //(set (get jane email) (eval (get bob email)))
//        ActionResponse janeEmailField = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "jane", "email");
//        if (janeEmailField.isSuccess())
//        {
//            ActionResponse bobEmailField = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "bob", "email");
//
//            if (bobEmailField.isSuccess())
//            {
//                ActionResponse bobEmailFieldVal = allUserActions.evalField(new InfoForCommand(userSays, null), bobEmailField.getField());
//
//                if (bobEmailFieldVal.isSuccess())
//                {
//                    actionResponse = allUserActions.setFieldFromFieldVal(new InfoForCommand(userSays, null), janeEmailField.getField(), bobEmailFieldVal.getValue());
//                }
//            }
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "set the recipient to be bob's email";
        testHelpers.userSays(userSays);
        //parser should translate to:
        //(set (get recipient_list) (eval (get bob email)))
//        ActionResponse recipientField = allUserActions.getProbFieldByFieldName(new InfoForCommand(userSays, null), "recipient list");
//        if (recipientField.isSuccess())
//        {
//            ActionResponse bobEmailField = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "bob", "email");
//
//            if (bobEmailField.isSuccess())
//            {
//                ActionResponse bobEmailFieldVal = allUserActions.evalField(new InfoForCommand(userSays, null) , bobEmailField.getField());
//
//                if (bobEmailFieldVal.isSuccess())
//                {
//                    actionResponse = allUserActions.setFieldFromFieldVal(new InfoForCommand(userSays, null), recipientField.getField(), bobEmailFieldVal.getValue());
//                }
//            }
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);


        //simple add test
        userSays = "add nana@gmail.com to the recipient list";
        testHelpers.userSays(userSays);
        //parser should translate to:
        //(add (get recipient_list) "nana@gmail.com")
//        ActionResponse recipientField = allUserActions.getProbFieldByFieldName(new InfoForCommand(userSays, null), "recipient list");
//        if (recipientField.isSuccess())
//        {
//            actionResponse = allUserActions.addToFieldFromString(new InfoForCommand(userSays, null), recipientField.getField(), "nana@gmail.com");
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);

        testHelpers.systemSays(response.sayToUser);
    }


    private static void teachingToSetRecipientAsContact(IAllUserActions allUserActions, TestHelpers testHelpers, ParserSettings parserSettings) throws InterruptedException
    {
        testHelpers.newSection("teachingToSetRecipientAsContact");

        //ActionResponse actionResponse;
        CcgUtils.SayAndExpression response;
        String userSays;

        userSays = "compose an email";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.createInstanceByConceptName(new InfoForCommand(userSays,null), "outgoing email");
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "set jane's email to be jane@gmail.com";
        //(set (get jane email) jane@gmail.com)
        testHelpers.userSays(userSays);
        //TODO: original sentence didn't work
        userSays = "set jane's email to jane@gmail.com";
//        actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "jane", "email");
//        if (response.isSuccess())
//        {
//            actionResponse = allUserActions.setFieldFromString(new InfoForCommand(userSays, null), response.getField(), "jane@gmail.com");
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "recipientify bob";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.set(new InfoForCommand(userSays,null), "recipient list", "bob");
        //actionResponse = allUserActions.unknownCommand(new InfoForCommand(userSays, null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "yes";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.yes(new InfoForCommand(userSays,null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "take bob's email";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "bob", "email");
//        if (response.isSuccess())
//        {
//            //maybe this should be done automatically every time.
//            actionResponse = allUserActions.evalField(new InfoForCommand(userSays, null), response.getField());
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "and set it as the recipient";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByFieldName(new InfoForCommand(userSays, null), "recipient list");
//        if (response.isSuccess())
//        {
//            //but then it will fail here...
//            actionResponse = allUserActions.setFieldFromPreviousEval(new InfoForCommand(userSays, null), response.getField());
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "that's it";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.end(new InfoForCommand(userSays, null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        //Thread.sleep(waitForLearning);

        userSays = "recipientify jane";
        testHelpers.userSays(userSays);
        //should now translate to:
//        {
//            actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "jane", "email");
//            if (actionResponse.isSuccess())
//            {
//                //maybe this should be done automatically every time.
//                ActionResponse fieldEval = allUserActions.evalField(new InfoForCommand(userSays, null), actionResponse.getField());
//
//                if (fieldEval.isSuccess())
//                {
//                    actionResponse = allUserActions.getProbFieldByFieldName(new InfoForCommand(userSays, null), "recipient list");
//                    if (actionResponse.isSuccess())
//                    {
//                        //but then it will fail here...
//                        actionResponse = allUserActions.setFieldFromFieldVal(new InfoForCommand(userSays, null), actionResponse.getField(), fieldEval.getValue());
//                    }
//                }
//            }
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

    }


    private static void buildRequiredDB(TopDMAllActions topDMAllActions, TestHelpers testHelpers, ParserSettings parserSettings)
    {
        testHelpers.newSection("buildRequiredDB");

        //ActionResponse actionResponse;
        CcgUtils.SayAndExpression response;
        String userSays;
        IAllUserActions allUserActions = topDMAllActions;
        IIncomingEmailControlling incomingEmailControlling = topDMAllActions;

        userSays = "create a contact my spouse";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.createInstanceByFullNames(new InfoForCommand(userSays, null), "contact", "my spouse");
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "set its email to my.spouse@gmail.com";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByFieldName(new InfoForCommand(userSays,null), "email");
//        if (response.isSuccess())
//            actionResponse = allUserActions.setFieldFromString(new InfoForCommand(userSays, null), response.getField(), "my.spouse@gmail.com");
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        incomingEmailControlling.addEmailMessageToInbox(new EmailInfo("bob7@myjob.com",
                "department party",
                Arrays.asList(new String[]{"you@myjob.com"}),
                new LinkedList<String>(),
                "We will have our department party next Wednesday at 4:00pm. Please forward this email to your spouse."
        ));

        incomingEmailControlling.addEmailMessageToInbox(new EmailInfo("dan@myjob.com",
                "another email",
                Arrays.asList(new String[]{"you@myjob.com"}),
                new LinkedList<String>(),
                "sending another email."
        ));
    }


    private static void learningToForwardAnEmail(IAllUserActions allUserActions, TestHelpers testHelpers, ParserSettings parserSettings) throws InterruptedException
    {
        testHelpers.newSection("learningToForwardAnEmail");

        //ActionResponse actionResponse;
        CcgUtils.SayAndExpression response;
        String userSays;

        userSays = "forward this email to my spouse";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.unknownCommand(new InfoForCommand(userSays, null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "sure";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.yes(new InfoForCommand(userSays, null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "first create a new email";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.createInstanceByConceptName(new InfoForCommand(userSays, null), "outgoing email");
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "then copy the subject from the incoming email to the outgoing email";//TODO: doesn't work
        userSays = "copy the subject";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.unknownCommand(new InfoForCommand(userSays, null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "take the subject from the incoming email";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "inbox", "subject");
//        if (response.isSuccess())
//            actionResponse = allUserActions.evalField(new InfoForCommand(userSays,null), response.getField());
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "and set it as the outgoing email's subject";
        //userSays = "and set it as the subject";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "outgoing email", "subject");
//        if (actionResponse.isSuccess())
//            actionResponse = allUserActions.setFieldFromFieldVal(new InfoForCommand(userSays, null), actionResponse.getField(), allUserActions.getProbFieldVal(new InfoForCommand(userSays, null)).getValue());
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "take the body from the incoming email";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "inbox", "body");
//        if (response.isSuccess())
//            actionResponse = allUserActions.evalField(new InfoForCommand(userSays, null), response.getField());
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "and set it as the body";
        //userSays = "set it as the body";
        testHelpers.userSays(userSays);
        //actionResponse = allUserActions.getProbFieldByFieldName(new InfoForCommand(userSays, null), "body"); //should understand since incoming email should not be mutable, or maybe leave for parser
//        actionResponse = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "outgoing email", "body");
//        if (response.isSuccess())
//            actionResponse = allUserActions.setFieldFromPreviousEval(new InfoForCommand(userSays, null), response.getField());
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "send the email";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.sendEmail(new InfoForCommand(userSays,null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "recipientify my spouse";
        testHelpers.userSays(userSays);
        // should translate to: (set (get recipient_list) (eval (get my_spouse email)))
//        {
//            ActionResponse recipientField = allUserActions.getProbFieldByFieldName(new InfoForCommand(userSays, null), "recipient list");
//            if (recipientField.isSuccess())
//            {
//                ActionResponse spouseEmailField = allUserActions.getProbFieldByInstanceNameAndFieldName(new InfoForCommand(userSays, null), "my spouse", "email");
//
//                if (spouseEmailField.isSuccess())
//                {
//                    ActionResponse spouseEmailFieldVal = allUserActions.evalField(new InfoForCommand(userSays, null), spouseEmailField.getField());
//
//                    if (spouseEmailFieldVal.isSuccess())
//                    {
//                        actionResponse = allUserActions.setFieldFromFieldVal(new InfoForCommand(userSays, null), recipientField.getField(), spouseEmailFieldVal.getValue());
//                    }
//                }
//            }
//            testHelpers.systemSays(actionResponse.getSayToUserOrExec());
//        }
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "now send the email";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.sendEmail(new InfoForCommand(userSays,null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        //userSays = "you're done"; //TODO: you're done doesn't work
        userSays = "finish";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.end(new InfoForCommand(userSays, null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        //Thread.sleep(waitForLearning);

        userSays = "forward this email to bob";
        testHelpers.userSays(userSays);
//        actionResponse = allUserActions.end(new InfoForCommand(userSays, null));
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

    }

    private static void smallUpdates(IAllUserActions allUserActions, TestHelpers testHelpers, ParserSettings parserSettings)
    {
        testHelpers.newSection("smallUpdates");
        String userSays;
        //ActionResponse response;
        CcgUtils.SayAndExpression response;

        userSays = "next message";
        testHelpers.userSays(userSays);
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "read email";
        testHelpers.userSays(userSays);
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "add tasks to contact";
        testHelpers.userSays(userSays);
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        //tasks is plural, so will be assigned a list
        userSays = "set bob's tasks to complaints dealing";
        testHelpers.userSays(userSays);
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "add helping new workers to bob's tasks";
        testHelpers.userSays(userSays);
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

        userSays = "what are bob's tasks";
        testHelpers.userSays(userSays);
        response = parserSettings.parseAndEval(allUserActions, userSays);
        testHelpers.systemSays(response.sayToUser);

    }

    static class TestSimplifier
    {
        IAllUserActions allUserActions;
        TestHelpers testHelpers;
        ParserSettings parserSettings;
        public TestSimplifier(IAllUserActions allUserActions, TestHelpers testHelpers, ParserSettings parserSettings)
        {
            this.allUserActions = allUserActions;
            this.testHelpers = testHelpers;
            this.parserSettings = parserSettings;
        }

        public void userSays(String[] sentences)
        {
            Arrays.asList(sentences).stream().forEach(this::userSays);
        }

        public void userSays(String userSays)
        {
            testHelpers.userSays(userSays);
            CcgUtils.SayAndExpression response = parserSettings.parseAndEval(allUserActions, userSays);
            testHelpers.systemSays(response.sayToUser);
        }

        public void newSection(String sectionName)
        {
            testHelpers.newSection(sectionName);
        }

    }

    static class TestHelpers
    {
        boolean testingMode;
        StringBuilder allSystemReplies;
        String fileName;

        public TestHelpers(boolean testingMode, String fileName)
        {
            this.testingMode = testingMode;
            allSystemReplies = new StringBuilder();
            this.fileName = fileName;
        }

        public void userSays(String str)
        {
            StringBuilder toOutput = new StringBuilder();
            String[] sentences = str.split("\n");
            for (String sentence : sentences)
            {
                toOutput.append("U: " + sentence + "\n");
            }
            System.out.print(toOutput.toString());
            allSystemReplies.append(toOutput.toString());
        }

        public void newSection(String sectionName)
        {
            String outputStr = "---------------- now in " + sectionName + " --------------------\n";
            System.out.println(outputStr);
            allSystemReplies.append(outputStr + "\n");
        }

        public void systemSays(String str)
        {
            StringBuilder toOutput = new StringBuilder();
            String[] sentences = str.split("\n");
            for (String sentence : sentences)
            {
                toOutput.append("S: " + sentence + "\n");
            }
            toOutput.append("\n");
            System.out.print(toOutput.toString());
            allSystemReplies.append(toOutput.toString());
        }

        public void endTest() throws Exception
        {
            if (testingMode)
            {
                String shouldBe = StringUtils.join(Files.readAllLines(Paths.get(fileName)), "\n");
                if (!shouldBe.equals(allSystemReplies.toString()))
                {
                    String failFileName = new SimpleDateFormat("yyyyMMddhhmm'fail.txt'").format(new Date());
                    PrintWriter out = new PrintWriter(failFileName);
                    out.println(allSystemReplies.toString());
                    out.close();
                    System.out.println("Error!!!!\nTest failed!!!\n");
                    throw new Exception("Test failed");
                }
                else
                {
                    System.out.println("Success!!!!\n");
                }
            }
            else
            {
                PrintWriter out = new PrintWriter(fileName);
                out.println(allSystemReplies.toString());
                out.close();
            }
        }
    }
}
