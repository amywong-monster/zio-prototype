# ZIO prototype

## Run unit-test
```
sbt test
```
It should return something like
```
[info] + Config
[info]   + Config.Loader
[info]     + test `load` function
[info] Ran 1 test in 1 s 166 ms: 1 succeeded, 0 ignored, 0 failed
[info] Done
```

## Run integration test
```
docker run -dit --name=postgres-zio-prototype -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=postgres postgres
```
```
sbt "it:test"
```
It should return something like
```
[info] + LinkStore
[info]   + LinkStore.Service
[info]     + test `add` function - should add 1 link and handle uniqueKey violation or retrieve the link correctly
[info] Ran 1 test in 3 s 157 ms: 1 succeeded, 0 ignored, 0 failed
[info] Done
```