### Expirable

Package contains some abstractions to manage object lifetime.

```sh
gradle build
gradle test
```

This pattern is used in the `IHttpClientFactory` on `dotnet` platform that manages instantiation of `HttpClient` objects. Every time the user calls `IHttpClientFactory.CreateClient` with the same key, they get different `HttpClient` instances that reuse the same `HttpMessageHandler` until the last one expires
