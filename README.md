# Porg
A MapReduce framework 

## What can it do?
Porg is designed to run MapReduce program on the Internet. It simply replaces Task Nodes (in Hadoop) with Browsers. As it uses browsers, its task nodes can be easily extended. Therefore every client on the Internet can be a task node. Besides, Porg uses C++ as main language so that it can be compiled into WebAssembly which has a better performance than JavaScript.

## Why does it use Browser?
Firstly, Browser is an easy solution for cross-platform. Windows, Mac, even Android can easily be a task node. Secondly, almost every device has a browser. So they can just simply type in the URL of the job to be a task node without installing other applications. In addition, as we uses JavaScript as a part of the system, maybe one day we will consider running the program on node.js which can use local file system as a buffer.

## Progress
    * v0.1 is under development.
