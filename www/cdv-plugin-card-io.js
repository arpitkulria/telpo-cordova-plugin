
function MagneticCardHelper() {

}



//--------------------------------------------------FIRST---------------------------------------------------------------
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


MagneticCardHelper.prototype.startReading = function() {
  console.log("-----------In OPEN FUNCTION---------------------------");
    var failureCallback = function() {
        console.log("Problem while startReading");
    };

    var wrappedSuccess = function() {
        console.log("startReading success");
    };
  cordova.exec(wrappedSuccess, failureCallback, "MagneticCardHelper", "startReading", []);
};

//----------------------------------------------------------------------------------------------------------------------
module.exports = new MagneticCardHelper();
