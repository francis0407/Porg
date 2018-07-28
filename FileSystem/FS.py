import os
import random
import sys
#flag = 0, start to end; flag = 1, end to start  

#python test.py E:/Porg/input.txt b E:/Porg/
def find(string, flag, char):
	length = len(string)
	if flag == 0:
		for i in range(0, length):
			if(string[i] == char or string[i] == '\0'):
				return i
	else:
		for i in range(0, length):
			if(string[length - i - 1] == char or string[length - i - 1] == '\0'):
				return i
	return -1

def partition(path, star, output):
	global start
	global length
	size = os.path.getsize(path)
	f = open(path, "r")
	i = 0
	slice = 10
	piece = 30
	while(start[i] <= size-1):
		if(size - start[i] <= piece):
			break
		if i % 2 == 0:      #down
			bias = -1
			j = 0
			while(bias < 0):
				f.seek(start[i] + piece + slice*j, 0)
				if(size - start[i] - piece - slice*j > slice):
					ipt = f.read(slice)
				else:
					bias = -1
					break
				bias = find(ipt, 0, star)
				j += 1
			if(bias == -1):
				break
			length.append(piece + bias + slice*(j-1) + 1)
			start.append(start[i] + length[i])
		else:
			bias = -1
			j = 1
			flag = 1
			while(bias < 0):
				if(flag == 1 and piece - slice*j < 0):
					flag = 0
					j = 0
				if(flag == 1):   
					f.seek(start[i] + piece - slice*j, 0)
					if(size - piece*i > slice):
						ipt = f.read(slice)
					else:
						bias = -1
						break
					bias = find(ipt, 1, star)
				else:
					f.seek(start[i] + piece + slice*j, 0)
					if(size - start[i] - piece - slice*j > slice):
						ipt = f.read(slice)
					else:
						bias = -1
						break
					bias = find(ipt, 0, star)
				j += 1
			if(bias == -1):
				break
			if(flag == 1):
				length.append(piece - bias - slice*(j-2))
				start.append(start[i] + length[i])
			else:
				length.append(piece + bias + slice*j + 1)
				start.append(start[i] + length[i])
		i += 1
	length.append(size - start[i-1])
	f.close()
	f = open(output + "start.txt", "w")
	for i in range(0, len(start)):
		f.write(str(start[i]))
		f.write("\n")
	f.close()
	f = open(output + "length.txt", "w")
	for i in range(0, len(length)):
		f.write(str(length[i]))
		f.write("\n")
	f.close()

def info(input, path):
	global start
	global length
	f = open(input, "r")
	l = len(start)
	for i in range(0, l):
		f1 = open(path + str(i) + '.txt', "w")
		f.seek(start[i])
		s = f.read(length[i])
		f1.write(s)
		f1.close()

start = []
start.append(0)
length = []
partition(sys.argv[1], sys.argv[2], sys.argv[3])
info(sys.argv[1], sys.argv[3])
