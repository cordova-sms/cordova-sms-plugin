module.exports = {

    send: function (win, fail, args) {

        var chatMessage = new Windows.ApplicationModel.Chat.ChatMessage();
        chatMessage.body = args[1];
        const validRecipent = args[0] && args[0].every(function (r) {
            return !r == false;
        });
        if (validRecipent) {
            chatMessage.recipients.push(args[0]);
        }
        Windows.ApplicationModel.Chat.ChatMessageManager.showComposeSmsMessageAsync(chatMessage).done(win, fail);

    }
};

require("cordova/exec/proxy").add("Sms", module.exports);