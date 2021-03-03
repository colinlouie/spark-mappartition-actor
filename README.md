# Spark interaction with Actors

I wrote this guide for those new to using Akka Actors in Apache Spark.

This is not meant to be a guide for Apache Spark, or Akka Actors. Only the integration thereof.

# Building

`sbt assembly`

# Running

`spark-submit target/scala-2.12/spark-mappartition-actor-assembly-0.1.jar`

# Sample output

```
>>>>> Instantiating Actor: akka://main/user/hello2
>>>>> Instantiating Actor: akka://main/user/hello0
>>>>> Instantiating Actor: akka://main/user/hello3
>>>>> Instantiating Actor: akka://main/user/hello1
>>>>> Instantiating Actor: akka://main/user/hello4
akka://main/user/hello1 is processing: Record(hello7,a7,bb7,86)
akka://main/user/hello2 is processing: Record(hello2,a2,bb2,79)
akka://main/user/hello4 is processing: Record(hello4,a4,bb4,82)
akka://main/user/hello0 is processing: Record(hello5,a5,bb5,80)
akka://main/user/hello3 is processing: Record(hello1,a1,bb1,86)
akka://main/user/hello2 is processing: Record(hello3,a3,bb3,58)
akka://main/user/hello0 is processing: Record(hello9,a9,bb9,82)
akka://main/user/hello4 is processing: Record(hello8,a8,bb8,99)
akka://main/user/hello2 is processing: Record(hello6,a6,bb6,93)
pong!
pong!
pong!
pong!
pong!
pong!
pong!
pong!
pong!
```
