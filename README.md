# Uberv2

Now, You can run UberProducerConsumer class in tow ways:

1)...... UberProducerConsumer true ....
Here, you are calling the class "UberProducerConsumer" with an argument "true". that means if it finds an argument "true" then it will call UberData class. which
produces data to confluent kafka.

You can see the code of that:


object UberProducerConsumer extends App with Serializable {

  if (args(0) == "true") { // It checks wether the argument is "true" or not, if it's true then it calls class UberData class which produces data to confluent kafka server.
  println("Calling producer.....")
    UberData
  }

2) ...... UberProducerConsumer..........

Here, you are calling the class "UberProducerConsumer" without any argument. that means if it doesn't find any argument then it will not call UberData class for
producing any data to confluent kafka. 

You can see the code of that:


object UberProducerConsumer extends App with Serializable {

  if (args(0) == "true") { // It checks wether the argument is "true" or not, if it's not true then it goes for normal execution of the class UberProducerConsumer class.
  println("Calling producer.....")
    UberData
  }
