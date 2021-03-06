define([
    "dojo/_base/declare"
], function(declare) {
    return declare(null, {
        fromAnkor: function(ref) {
            return ref.getValue();
        },
        toAnkor: function(ref, value, eventSource) {
            ref.setValue(value, eventSource);
        }
    });
});
