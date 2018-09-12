from Porg import *

connectServer("ws://127.0.0.1:7272")
input_file = FileConf("PageRankExample/input","PageRankInput")
prog_file = FileConf("PageRankExample/PageRank.wasm","PageRankWASM")
job = Job("PageRank_0",prog_file,5,1024,input_file)
result = FileConf("")
for i in range(1,10):
    job.submitAndWait()
    result = job.getResult()
    job = Job("PageRank_"+str(i),prog_file,5,1024,result)