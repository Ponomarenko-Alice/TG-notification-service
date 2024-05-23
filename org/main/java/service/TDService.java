package service;

import models.ChannelPost;
import models.Link;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public final class TDService {
    private static final Long CHANNEL_ID = -1002021174198L;
    private static final ChannelPostService channelPostService = new ChannelPostService();
    private static final Client.ResultHandler defaultHandler = new DefaultHandler();
    private static final Lock authorizationLock = new ReentrantLock();
    private static final Condition gotAuthorization = authorizationLock.newCondition();
    private static final ConcurrentMap<Long, TdApi.User> users = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();
    private static final String newLine = System.lineSeparator();
    private static final Logger logger = Logger.getLogger(TDService.class.getName());
    private static Client client = null;
    private static TdApi.AuthorizationState authorizationState = null;
    private static volatile boolean haveAuthorization = false;
    private static volatile boolean needQuit = false;
    private static volatile boolean canQuit = false;
    private static volatile String currentPrompt = null;

    private static void print(String str) {
        if (currentPrompt != null) {
            System.out.println();
        }
        System.out.println(str);
        if (currentPrompt != null) {
            System.out.print(currentPrompt);
        }
    }

    private static void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        if (authorizationState != null) {
            TDService.authorizationState = authorizationState;
        }
        switch (TDService.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                TdApi.SetTdlibParameters request = new TdApi.SetTdlibParameters();
                request.databaseDirectory = "tdlib";
                request.useMessageDatabase = true;
                request.useSecretChats = true;
                request.apiId = 94575;
                request.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2";
                request.systemLanguageCode = "en";
                request.deviceModel = "Desktop";
                request.applicationVersion = "1.0";
                client.send(request, new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                //String token = "7172745077:AAHewi6x1Ykf6VQ--jZnyDNvz7FwLlHq_mY"; //for bot
                String phoneNumber = promptString("Please enter phone number: ");
                client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) TDService.authorizationState).link;
                System.out.println("Please confirm this login link on another device: " + link);
                break;
            }
            case TdApi.AuthorizationStateWaitEmailAddress.CONSTRUCTOR: {
                String emailAddress = promptString("Please enter email address: ");
                client.send(new TdApi.SetAuthenticationEmailAddress(emailAddress), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitEmailCode.CONSTRUCTOR: {
                String code = promptString("Please enter email authentication code: ");
                client.send(new TdApi.CheckAuthenticationEmailCode(new TdApi.EmailAddressAuthenticationCode(code)), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                String code = promptString("Please enter authentication code: ");
                client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
                String firstName = promptString("Please enter your first name: ");
                String lastName = promptString("Please enter your last name: ");
                client.send(new TdApi.RegisterUser(firstName, lastName, false), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
                String password = promptString("Please enter password: ");
                client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                haveAuthorization = true;
                authorizationLock.lock();
                try {
                    gotAuthorization.signal();
                } finally {
                    authorizationLock.unlock();
                }
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
                print("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
                print("Closing");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                print("Closed");
                if (!needQuit) {
                    client = Client.create(new UpdateHandler(), null, null); // recreate client after previous has closed
                } else {
                    canQuit = true;
                }
                break;
            default:
                System.err.println("Unsupported authorization state:" + newLine + TDService.authorizationState);
        }
    }

    private static String promptString(String prompt) {
        System.out.print(prompt);
        currentPrompt = prompt;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        try {
            str = reader.readLine();
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
        currentPrompt = null;
        return str;
    }

    private static void sendMessage(long chatId, String message) {
        // initialize reply markup just for testing
        TdApi.InlineKeyboardButton[] row = {new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())};
        TdApi.ReplyMarkup replyMarkup = new TdApi.ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][]{row, row, row});

        TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(message, null), null, true);
        client.send(new TdApi.SendMessage(chatId, 0, null, null, replyMarkup, content), defaultHandler);
    }

    private static void fillDataBaseUpdatedChannelPosts(TdApi.Messages messages) throws InterruptedException {
        List<Integer> arrayList = channelPostService.findAllChannelPosts().stream().map(ChannelPost::getId).collect(Collectors.toList());
        for (TdApi.Message message : messages.messages) {
            if (!arrayList.contains((int) message.id)) {
                channelPostService.save(new ChannelPost((int) message.id, false));
            }

            ChannelPost post = (ChannelPost) channelPostService.find((int) message.id);
            List<Link> linkList = post.getLinks();

            Thread.sleep(100);

            client.send(new TdApi.GetMessageThreadHistory(CHANNEL_ID, message.id, 1, -99, 100), resultThread -> {
                if (resultThread.getConstructor() == TdApi.Messages.CONSTRUCTOR) {
                    TdApi.Messages messageThread = (TdApi.Messages) resultThread;

                    List<Link> newLinks = new ArrayList<>();
                    for (TdApi.Message comment : messageThread.messages) {
                        if (comment.content instanceof TdApi.MessageText) {
                            TdApi.MessageText messageText = (TdApi.MessageText) comment.content;
                            String commentText = messageText.text.text;
                            Pattern pattern = Pattern.compile("https://[a-zA-Z0-9-.]+");
                            Matcher matcher = pattern.matcher(commentText);
                            while (matcher.find()) {
                                String tempLinkText = matcher.group();
                                if (!postHasParticularLinkText(post, tempLinkText)) {
                                    newLinks.add(new Link(tempLinkText, post));
                                }
                            }
                        }
                    }
                    synchronized (post) {
                        for (Link newLink : newLinks) {
                            if (!postHasParticularLinkText(post, newLink.getLinkText())) {
                                linkList.add(newLink);
                                post.setHasLink(true);
                            }
                        }
                        channelPostService.save(post);
                    }
                }
            });
        }
    }

    private static boolean postHasParticularLinkText(ChannelPost post, String linkText) {
        return post.getLinks().stream().anyMatch(link -> link.getLinkText().equals(linkText));
    }

    public static void scheduleTaskExecute() throws InterruptedException {
        Thread.sleep(1000);
        client.send(new TdApi.GetChatHistory(CHANNEL_ID, 1, -99, 100, false), result -> {
            if (result.getConstructor() == TdApi.Messages.CONSTRUCTOR) {
                try {
                    fillDataBaseUpdatedChannelPosts((TdApi.Messages) result);
                } catch (InterruptedException e) {
                    logger.warning(e.getMessage());
                }
            } else {
                logger.warning("Error fetching chat history: " + result.getConstructor());
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            LogManager.getLogManager().readConfiguration(
                    TDService.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            System.err.println("Could not setup logger configuration: " + e.toString());
        }
        // set log message handler to handle only fatal errors (0) and plain log messages (-1)
        Client.setLogMessageHandler(0, new LogMessageHandler());

        // disable TDLib log and redirect fatal errors and plain log messages to a file
        try {
            Client.execute(new TdApi.SetLogVerbosityLevel(0));
            Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile("tdlib.log", 1 << 27, false)));
        } catch (Client.ExecutionException error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }
        client = Client.create(new UpdateHandler(), null, null);

        while (!needQuit) {
            authorizationLock.lock();
            try {
                while (!haveAuthorization) {
                    gotAuthorization.await();
                }
            } finally {
                authorizationLock.unlock();
            }
            while (haveAuthorization) {
                scheduleTaskExecute();
                Thread.sleep(10000);
            }
        }
        while (!canQuit) {
            Thread.sleep(1);
        }
    }

    private static void onFatalError(String errorMessage) {
        final class ThrowError implements Runnable {
            private final String errorMessage;
            private final AtomicLong errorThrowTime;

            private ThrowError(String errorMessage, AtomicLong errorThrowTime) {
                this.errorMessage = errorMessage;
                this.errorThrowTime = errorThrowTime;
            }

            @Override
            public void run() {
                if (isDatabaseBrokenError(errorMessage) || isDiskFullError(errorMessage) || isDiskError(errorMessage)) {
                    processExternalError();
                    return;
                }

                errorThrowTime.set(System.currentTimeMillis());
                throw new ClientError("TDLib fatal error: " + errorMessage);
            }

            private void processExternalError() {
                errorThrowTime.set(System.currentTimeMillis());
                throw new ExternalClientError("Fatal error: " + errorMessage);
            }

            private boolean isDatabaseBrokenError(String message) {
                return message.contains("Wrong key or database is corrupted") || message.contains("SQL logic error or missing database") || message.contains("database disk image is malformed") || message.contains("file is encrypted or is not a database") || message.contains("unsupported file format") || message.contains("Database was corrupted and deleted during execution and can't be recreated");
            }

            private boolean isDiskFullError(String message) {
                return message.contains("PosixError : No space left on device") || message.contains("database or disk is full");
            }

            private boolean isDiskError(String message) {
                return message.contains("I/O error") || message.contains("Structure needs cleaning");
            }

            final class ClientError extends Error {
                private ClientError(String message) {
                    super(message);
                }
            }

            final class ExternalClientError extends Error {
                public ExternalClientError(String message) {
                    super(message);
                }
            }
        }

        final AtomicLong errorThrowTime = new AtomicLong(Long.MAX_VALUE);
        new Thread(new ThrowError(errorMessage, errorThrowTime), "TDLib fatal error thread").start();

        // wait at least 10 seconds after the error is thrown
        while (errorThrowTime.get() >= System.currentTimeMillis() - 10000) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class DefaultHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            print(object.toString());
        }
    }

    private static class UpdateHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                    onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                    break;

                case TdApi.UpdateUser.CONSTRUCTOR:
                    TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                    users.put(updateUser.user.id, updateUser.user);
                    break;
                case TdApi.UpdateUserStatus.CONSTRUCTOR: {
                    TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
                    TdApi.User user = users.get(updateUserStatus.userId);
                    synchronized (user) {
                        user.status = updateUserStatus.status;
                    }
                    break;
                }
                case TdApi.UpdateChatActionBar.CONSTRUCTOR: {
                    TdApi.UpdateChatActionBar updateChat = (TdApi.UpdateChatActionBar) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.actionBar = updateChat.actionBar;
                    }
                    break;
                }
                case TdApi.UpdateChatMessageSender.CONSTRUCTOR: {
                    TdApi.UpdateChatMessageSender updateChat = (TdApi.UpdateChatMessageSender) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.messageSenderId = updateChat.messageSenderId;
                    }
                    break;
                }
                case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR: {
                    TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.notificationSettings = update.notificationSettings;
                    }
                    break;
                }
                case TdApi.UpdateChatPendingJoinRequests.CONSTRUCTOR: {
                    TdApi.UpdateChatPendingJoinRequests update = (TdApi.UpdateChatPendingJoinRequests) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.pendingJoinRequests = update.pendingJoinRequests;
                    }
                    break;
                }
                case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: {
                    TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
                    }
                    break;
                }
                case TdApi.UpdateChatBackground.CONSTRUCTOR: {
                    TdApi.UpdateChatBackground updateChat = (TdApi.UpdateChatBackground) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.background = updateChat.background;
                    }
                    break;
                }
                case TdApi.UpdateChatTheme.CONSTRUCTOR: {
                    TdApi.UpdateChatTheme updateChat = (TdApi.UpdateChatTheme) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.themeName = updateChat.themeName;
                    }
                    break;
                }
                case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR: {
                    TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }

                default:
                    logger.warning("Unsupported update, " );
            }
        }
    }

    private static class AuthorizationRequestHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    System.err.println("Receive an error:" + newLine + object);
                    onAuthorizationStateUpdated(null); // repeat last action
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    // result is already received through UpdateAuthorizationState, nothing to do
                    break;
                default:
                    System.err.println("Receive wrong response from TDLib:" + newLine + object);
            }
        }
    }

    private static class LogMessageHandler implements Client.LogMessageHandler {
        @Override
        public void onLogMessage(int verbosityLevel, String message) {
            if (verbosityLevel == 0) {
                onFatalError(message);
                return;
            }
            System.err.println(message);
        }
    }
}
