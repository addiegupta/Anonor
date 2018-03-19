
let functions = require('firebase-functions');
let admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendPush = functions.https.onRequest((request,response) => {
    console.log('sendPush called');
	var	userCallerId = request.headers.user_id;
		
		console.log("UserCallerId",userCallerId)
	    
	    response.send(request.body.name +', Hello from Firebase!');

    return loadUsers().then(users => {
        let tokens = [];
        for (let user of users) {
        	if (user.fcm_token!==null) {
	            tokens.push(user.fcm_token);
    	        console.log("FCM TOKEN ",user.fcm_token);
        	}
        	else{
        		console.log("Empty token");
        	}
        }
        let payload = {
        				data: {
        						callerId: userCallerId
            				}
            		}
           
        console.log("userCallerId",userCallerId);

        if (tokens.length!==0) {
        	console.log("Returning tokens to push");

        	return admin.messaging().sendToDevice(tokens, payload);
        }
        else{
        	return null;
        }
    });
});

function loadUsers() {
    let dbRef = admin.database().ref('/users');
    let defer = new Promise((resolve, reject) => {
        dbRef.once('value', (snap) => {
            let data = snap.val();
            let dbSize = snap.numChildren();

            let users = [];

            if (dbSize <= 10) {
            	for(var user in data){
            		if(data[user].call_request!=="true" && data[user].fcm_token){

            		users.push(data[user]);
            		console.log("pushing user ", data[user]);
            		}
            	}
            }
            else{
				while(users.length < 10){
    				var randomnumber = Math.ceil(Math.random()*dbSize)
    				console.log("random",randomnumber);
    				if(users.indexOf( randomnumber) > -1) continue;

					if(data[randomnumber].call_request!=="true"  && data[randomnumber].fcm_token){
            	
    				users[users.length] = data[randomnumber];
    				console.log("more than 10,pushing",data[randomnumber]);
					}
				}
            }
            resolve(users);
        }, (err) => {
            reject(err);
        });
    });
    return defer;
}
