define([
    "./Message",
    "../Path",
    "../events/BaseEvent",
    "../events/ChangeEvent",
    "../events/ActionEvent"
], function(Message, Path, BaseEvent, ChangeEvent, ActionEvent) {
    var BaseTransport = function() {
        this.outgoingMessages = [];
        this.messageCounter = 0;
    };

    BaseTransport.prototype.init = function(ankorSystem) {
        this.ankorSystem = ankorSystem;
        this.utils = ankorSystem.utils;
    };

    BaseTransport.prototype.sendEvent = function(event) {
        var messageId = this.ankorSystem.senderId + "#" + this.messageCounter++;
        var message = new Message(this.ankorSystem.senderId, this.ankorSystem.modelId, messageId, event);
        this.sendMessage(message);
    };

    BaseTransport.prototype.sendMessage = function(message) {
        this.outgoingMessages.push(message);
        if (this.ankorSystem.debug) {
            console.log("OUT", message);
        }
    };

    BaseTransport.prototype.receiveMessage = function(message) {
        if (this.ankorSystem.debug) {
            console.log("IN", message);
        }
        this.ankorSystem.onIncomingEvent(message.event);
    };

    BaseTransport.prototype.decodeMessage = function(parsedJson) {
        var event = null;
        var path = new Path(parsedJson.property);
        if (parsedJson.change) {
            event = new ChangeEvent(path, "ankorRemoteEvent", parsedJson.change.type, parsedJson.change.key, parsedJson.change.value);
        }
        else if (parsedJson.action) {
            event = new ActionEvent(path, "ankorRemoteEvent", parsedJson.action);
        }
        else {
            event = new BaseEvent(path, "ankorRemoteEvent");
        }

        return new Message(parsedJson.senderId, parsedJson.modelId, parsedJson.messageId, event);
    };

    BaseTransport.prototype.encodeMessage = function(message) {
        var event = message.event;
        var jsonMessage = {
            senderId: message.senderId,
            modelId: message.modelId,
            messageId: message.messageId,
            property: event.path.toString()
        };
        if (event instanceof ActionEvent) {
            jsonMessage.action = event.action;
        }
        else if (event instanceof ChangeEvent) {
            jsonMessage.change = {
                type: event.type,
                key: event.key,
                value: event.value
            };
        }
        return jsonMessage;
    };

    return BaseTransport;
});
