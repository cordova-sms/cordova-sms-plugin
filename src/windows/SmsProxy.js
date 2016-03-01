module.exports = {

    send: function (win, fail, args) {

        var chatMessage = new Windows.ApplicationModel.Chat.ChatMessage();
        chatMessage.body = args[1];
        chatMessage.recipients.push(args[0]);
        Windows.ApplicationModel.Chat.ChatMessageManager.showComposeSmsMessageAsync(chatMessage);

    }
};

require("cordova/exec/proxy").add("Sms", module.exports);
