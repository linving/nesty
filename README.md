# Nesty
Http RESTful Api implemention on Netty async io

## Lastest version
0.0.2

## changeslog 

0.0.2
* support http long connection(Connection: keep-alive)
* support root path (uri path is /) stats and counter information

0.0.1 
* original snapshot version

## Features

* Http HTTP/1.1 protocol support 

GET | POST | UPDATE | DELETE
--- | --- | --- | ---

* Http Restful serialized (usually as json) in string body (With Gson)
* Http Short Connection on async mode by default (With Netty 4.2)
* Http request mapping variable support

Annotation | From 
--- | --- 
@Header | http header 
@RequestParam | http url query string or http body key value pairs 
@PathVariabl | http uri path vairable with {path} 
@Body | http body 

* Http request mapping method params type support

Class Type | Default value (require = false is set) | Description
--- | --- | --- 
int,short,long | 0 | primitive
float,double | 0.0d | primitive
String | null | string value
Enum | null | enum class type
Class | null | from http body serializer parsed

## TODO
* Long connection support (require explict Connection: Keep-Alive header set)
* Spring or Mybatis intergrated


## Usage

* Simplest http server

```java
public class SimpleHttpServer {

	public static void main(String[] args) throws ControllerRequestMappingException {
		AsyncServerProvider.builder().port(8080).service(NestyProtocol.HTTP)
			.scanHttpController("org.nesty.example.httpserver.handler").start();
	}
}
```

* Normal http server

```java

public static void main(String[] args) {

	// 1. build httpserver
	NestyServer server = AsyncServerProvider.builder().port(8080).service(NestyProtocol.HTTP);

	// 2. choose http params. this is unnecessary
	server.option(NestyOptions.IO_THREADS, Runtime.getRuntime().availableProcessors())
		  .option(NestyOptions.WORKER_THREADS, 128)
		  .option(NestyOptions.TCP_BACKLOG, 1024)
		  .option(NestyOptions.TCP_NODELAY, true);

	// 3. scan defined controller class with package name
	server.scanHttpController("com.nesty.test.neptune")
		  .scanHttpController("com.nesty.test.billing")
		  .scanHttpController("org.nesty.example.httpserver.handler");

	// 4. start http server
	if (!server.start())
		System.err.println("NestServer run failed");

	try {
		// join and wait here
		server.join();
		server.shutdown();
	} catch (InterruptedException ignored) {
	}
}
```

* Controlloer

```java
@Controller
@RequestMapping("/projects")
public class ServiceController {

	@RequestMapping(value = "/{projectId}", method = RequestMethod.GET)
	public ServiceResponse getProjectById(@PathVariable("projectId") Integer projectId) {
		System.out.println("getProjectById() projectId " + projectId);
		return new ServiceResponse();
	}
}

```

* Interceptor

```java
@Interceptor
public class ServiceInterceptor extends HttpInterceptor {

	@Override
	public boolean filter(final HttpContext context) {

		// count the request
		totalRequest.incrementAndGet();

		// show remote address
		if (!context.getRemoteAddress().isEmpty())
			System.out.println(String.format("request from client %s", context.getRemoteAddress()));

		// reject some one which is not we want
		if (context.getRemoteAddress().equals("192.168.1.1"))
			return false;

		// reject request from agent like curl
		if (context.getHttpHeaders().containsKey("User-Agent"))
			return false;

		// OK
		return true;
	}   

	@Override
	public DefaultFullHttpResponse handler(final HttpContext context, DefaultFullHttpResponse response) {

		// compress content if client support
		if ("gzip".equalsIgnoreCase(context.getHttpHeaders().get("Accept-Encoding"))) {
			// compress the body
			ByteBuf compressContent = compressWithGzip(response.content());
			DefaultFullHttpResponse newResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, compressContent);
			response = newResponse;
		}   

		// add more header
		response.headers().add("NestyInceptor", "Nesty is Good");

		return response;
	}   
}
```


* More examples 

Please visit https://github.com/gugemichael/nesty/wiki/More-Examples

## Threads Model

* Netty Bootstrap(io threads) + ThreadPool(logic threads)

![screenshot](http://img1.tbcdn.cn/L1/461/1/40ef4fb553fb9b565ddf79989a6f17877dcb3de7)

## Performance

java -server -Xmx4G -Xms4G -Xmn1536M -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:PermSize=256m -XX:MaxPermSize=256m -XX:+DisableExplicitGC

Http short connection
* Conccurent : 512 http connections 
* Qps : 40,000+
* Latency : < 10ms

Http long connection (Connection: keep-alive)
* Conccurent : 512 http connections 
* Qps : 80,000 ~ 100,000
* Latency : < 50ms

detail : https://github.com/gugemichael/nesty/wiki/Performance-Detail

