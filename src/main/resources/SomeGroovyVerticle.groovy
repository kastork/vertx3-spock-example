
println "will run on 8080"

vertx.createHttpServer().requestHandler({ req ->
  req.response()
      .putHeader("content-type", "text/html")
      .end("<html><body><h1>Hello from vert.x!</h1></body></html>")
}).listen(8080)

println "running on 8080"