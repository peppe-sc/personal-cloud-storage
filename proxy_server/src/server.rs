use std::{
    collections::{HashMap},

};

enum Actors{
    CLIENT(usize),
    SERVER(usize)
}

use actix::prelude::*;
use rand::{rngs::ThreadRng, Rng};

/// Chat server sends this messages to session
#[derive(Message)]
#[rtype(result = "()")]
pub struct Message(pub String);

/// Message for chat server communications

/// New chat session is created
#[derive(Message)]
#[rtype(usize)]
pub struct Connect {
    pub addr: Recipient<Message>,
}

/// Send message to specific room
#[derive(Message)]
#[rtype(result = "()")]
pub struct ClientMessage {
    /// Id of the client session
    pub id: usize,
    /// Peer message
    pub msg: String,

}

/// Session is disconnected
#[derive(Message)]
#[rtype(result = "()")]
pub struct Disconnect {
    pub id: usize,
}

/// `ProxyServer` manages the socket and responsible for coordinating sessions.
///
/// Implementation is very naïve.
#[derive(Debug)]
pub struct ProxyServer {
    sessions: HashMap<usize, Recipient<Message>>,
    rng: ThreadRng,
    
}

impl ProxyServer {
    pub fn new() -> ProxyServer {
        
        ProxyServer {
            sessions: HashMap::new(),
            rng: rand::thread_rng(),
        }

    }
}

impl ProxyServer {
    /// Send message to all users in the room
    fn send_message(&self, message: &str, id: usize) {

        let receiver = self.sessions.keys().find(|&&x|{x != id});

        if let Some(receiver_id) = receiver {

            let addr = self.sessions.get(receiver_id);

            addr.unwrap().do_send(Message(message.to_owned()));
        }

        
    }
}

/// Make actor from `ChatServer`
impl Actor for ProxyServer {
    /// We are going to use simple Context, we just need ability to communicate
    /// with other actors.
    type Context = Context<Self>;
}

/// Handler for Connect message.
///
/// Register new session and assign unique id to this session
impl Handler<Connect> for ProxyServer {
    type Result = usize;

    fn handle(&mut self, msg: Connect, _: &mut Context<Self>) -> Self::Result {
        println!("Someone joined");

        // notify all users in same room
        //self.send_message("Connected",  0);

        // register session with random id
        let id = self.rng.gen::<usize>();
        self.sessions.insert(id, msg.addr);

        // send id back
        id
    }
}

/// Handler for Disconnect message.
impl Handler<Disconnect> for ProxyServer {
    type Result = ();

    fn handle(&mut self, msg: Disconnect, _: &mut Context<Self>) {
        println!("Someone disconnected");

        // remove address
        self.sessions.remove(&msg.id);

    }
}

impl Handler<ClientMessage> for ProxyServer {
    type Result = ();

    fn handle(&mut self, msg: ClientMessage, _: &mut Context<Self>) {
        self.send_message(msg.msg.as_str(), msg.id);
    }
}