### Expirable

Package contains some abstractions to manage object lifetime.

```sh
gradle test
```

This pattern is used in the `IHttpClientFactory` on `dotnet` platform that manages instantiation of `HttpClient` objects.

Suppose you have some object that can be reused across multiple holders. To target this aim, you put that object
into `ExpirableCollection` and specify how long its lifetime is.

In the example above, `HttpMessageHandler` plays the role of an expensive and reusable object. So every time the user calls `IHttpClientFactory.CreateClient` with the same key, they get different `HttpClient` instances that reuse the same `HttpClientHandler` until the last one expires
