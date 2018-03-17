
let functions = require('firebase-functions');
let admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendPush = functions.https.onRequest((request,response) => {
    console.log('sendPush called');
	var userCallerId;

 	if (!request.headers.authorization) {
      console.error('No Firebase ID token was passed');
      response.status(403).send('Unauthorized');
      return;
 	}
	admin.auth().verifyIdToken(request.headers.authorization).then(decodedIdToken => {
	    console.log('ID Token correctly decoded', decodedIdToken);
		userCallerId = decodedIdToken.user_id;
		console.log("UserCallerId",userCallerId)
	    
	    request.user = decodedIdToken;
	    response.send(request.body.name +', Hello from Firebase!');

		return null;

	}).catch(error => {
	    console.error('Error while verifying Firebase ID token:', error);
	    response.status(403).send('Unauthorized');
	});

    return loadUsers().then(users => {
        let tokens = [];
        for (let user of users) {
        	if (user.fcm_token!==null) {
	            tokens.push(user.fcm_token);
    	        console.log(user.fcm_token);
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
    let dbRef = admin.database().ref('/receivers');
    let defer = new Promise((resolve, reject) => {
        dbRef.once('value', (snap) => {
            let data = snap.val();
            let dbSize = snap.numChildren();

            let users = [];

            if (dbSize <= 10) {
            	for(var user in data){
            		users.push(data[user]);
            		console.log("pushing user ", data[user]);
            	}
            }
            else{
				while(users.length < 10){
    				var randomnumber = Math.ceil(Math.random()*dbSize)
    				console.log("random",randomnumber);
    				if(users.indexOf( randomnumber) > -1) continue;


    				users[users.length] = data[randomnumber];
    				console.log("more than 10,pushing",data[randomnumber]);
				}
            }
            resolve(users);
        }, (err) => {
            reject(err);
        });
    });
    return defer;
}
