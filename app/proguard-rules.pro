### RxJava, RxAndroid (https://gist.github.com/kosiara/487868792fbd3214f9c9)
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
