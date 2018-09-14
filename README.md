naivechain-kotlin
===

```shell
$ ./gradlew jar
$ java -jar ./nativechain/build/libs/nativechain-1.0.jar 3001 6001
$ java -jar ./nativechain/build/libs/nativechain-1.0.jar 3002 6002
```

```shell
# add peer
$ curl -H "Content-type:application/json" --data '{"host":"http://127.0.0.1:6002"}' http://localhost:3001/peers

# add new block
$ curl -H "Content-type:application/json" --data '{"data":"second block"}' http://127.0.0.1:3001/mine
```
