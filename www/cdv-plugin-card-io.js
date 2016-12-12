
function MagneticCardHelper() {

}

function SmartCardHelper() {

}

MagneticCardHelper.prototype.open = function() {
    var failureCallback = function() {
        console.log("Problem while open");
    };

    var wrappedSuccess = function() {
        console.log("Open success");
    };

    cordova.exec(wrappedSuccess, failureCallback, "MagneticCardHelper", "open", []);
};

MagneticCardHelper.prototype.startReading = function(callback) {
    var failureCallback = function() {
        console.log("Problem while startReading");
    };

    cordova.exec(callback, failureCallback, "MagneticCardHelper", "startReading", []);
};


SmartCardHelper.prototype.startMonitor = function(callback) {
    var failureCallback = function() {
        console.log("Problem while startReading");
    };

    cordova.exec(callback, failureCallback, "SmartCardHelper", "startMonitor", []);
};


module.exports = new MagneticCardHelper();
module.exports = new SmartCardHelper();
