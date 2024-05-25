use actix::{Actor, StreamHandler};
use actix_web::{get, post, web, App, HttpResponse, HttpServer, Responder, HttpRequest, Error};
use actix_cors::Cors;
use actix_web_actors::ws;

struct MyWs;

impl Actor for MyWs {
    type Context = ws::WebsocketContext<Self>;
}

/// Handler for ws::Message message
impl StreamHandler<Result<ws::Message, ws::ProtocolError>> for MyWs {
    fn handle(&mut self, msg: Result<ws::Message, ws::ProtocolError>, ctx: &mut Self::Context) {
        match msg {
            Ok(ws::Message::Ping(msg)) => ctx.pong(&msg),
            Ok(ws::Message::Text(text)) =>{println!("{:?}",text); ctx.text(text)},
            Ok(ws::Message::Binary(bin)) => ctx.binary(bin),
            _ => (),
        }
    }
}

async fn index(req: HttpRequest, stream: web::Payload) -> Result<HttpResponse, Error> {
    let resp = ws::start(MyWs {}, &req, stream);
    println!("{:?}", resp);
    resp
}


#[get("/")]
async fn hello() -> impl Responder {
    HttpResponse::Ok().body("Hello world!")
}

#[post("/echo")]
async fn echo(req_body: String) -> impl Responder {
    HttpResponse::Ok().body(req_body)
}

async fn manual_hello() -> impl Responder {
    HttpResponse::Ok().body("Hey there!")
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    
    HttpServer::new(|| {
        let cors = Cors::permissive();
        App::new()
            .wrap(cors)
            .route("/ws/", web::get().to(index))
            .service(hello)
            .service(echo)
            .route("/hey", web::get().to(manual_hello))
    })
    .bind((/*"172.31.3.97"*/"localhost", 80))?
    .run()
    .await
}