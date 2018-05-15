const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendCommentNotifictions = functions.firestore
    .document(`/timelines/{receiver_id}/activities/{activity_id}`).onWrite((change, context) => {
        const user_id = context.params.user_id;
        const activity_id = context.params.activity_id;
        const receiver_id = context.params.receiver_id;
        const type = context.params.type;
        const post_id = context.params.post_id;


        console.log('the receiver id is;', receiver_id);

    })

exports.sendLikeNotifictions = functions.firestore
    .document(`/timelines/{receiver_id}/activities/{post_id}`).onWrite((change, context) => {
        const user_id = context.params.user_id;
        const activity_id = context.params.activity_id;
        const receiver_id = context.params.receiver_id;
        const type = context.params.type;
        const post_id = context.params.post_id;

        console.log('the activity id is;', activity_id);

        const deviceRef = admin.firestore().collection('users');
        const query = deviceRef.where('user_id', '==', receiver_id).get()
            .then(snapshot => {
                snapshot.forEach(document => {

                })


            })

    })