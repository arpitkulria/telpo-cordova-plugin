
function MagneticCardHelper() {

}

MagneticCardHelper.prototype.open = function() {
    console.log("-----------In OPEN FUNCTION---------------------------");
    var failureCallback = function() {
        console.log("Problem while open");
    };

    var wrappedSuccess = function() {
        console.log("Open success");
    };

    cordova.exec(wrappedSuccess, failureCallback, "MagneticCardHelper", "open", []);
};

MagneticCardHelper.prototype.startReading = function(callback) {
    console.log("-----------In startReading FUNCTION---------------------------");

    var failureCallback = function() {
        console.log("Problem while startReading");
    };

    cordova.exec(callback, failureCallback, "MagneticCardHelper", "startReading", []);
};


module.exports = new MagneticCardHelper();
