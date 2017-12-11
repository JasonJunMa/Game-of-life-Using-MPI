from mpi4py import MPI
import random
import sys
import time

COMM_WORLD = MPI.COMM_WORLD
rank = COMM_WORLD.Get_rank()
size = COMM_WORLD.Get_size()
boardSize = 50
generations = 1000
sliceNum = boardSize / size
tag = 10

board = []
def printBoard():
	for i in range(boardSize):
		for j in range(boardSize):
			if board[i][j] == 1:
				sys.stdout.write('*')
				sys.stdout.flush()
			else:
				sys.stdout.write(' ')
				sys.stdout.flush()
		print ""

myslice = []
for i in range(sliceNum):
	column = []
	for j in range(boardSize):
		column.append(0)
	myslice.append(column)


if rank==0:
	#init board with randon 0 and 1
	for i in range(boardSize):
		column = []
		for j in range(boardSize):
			column.append(random.randint(0,1))
		board.append(column)

	#Store for the first node
	for i in range(sliceNum):
		for j in range(boardSize):
			myslice[i][j] = board[i][j]

	#create a send slice
	sendSlice = []
	for i in range(sliceNum):
		column = []
		for j in range(boardSize):
			column.append(0)
		sendSlice.append(column)

	for i in range(1,size):
		for j in range(sliceNum):
			for k in range(boardSize):
				sendSlice[j][k] = board[j+i*sliceNum][k]
		COMM_WORLD.send(sendSlice,dest=i,tag=10)
else:
	myslice = COMM_WORLD.recv(source = 0,tag=10)

todown = []
toup = []
fromdown = []
fromup = []
for i in range(boardSize):
	 todown.append(0)
	 toup.append(0)
	 fromdown.append(0)
	 fromup.append(0)

toDownRank = 0
toUpRank = 0
fromDownRank = 0
fromUpRank = 0
g = 0

while(g<generations):
	if rank!=size-1:
		toDownRank = rank+1
	else:
		toDownRank = 0

	for i in range(boardSize):
		todown[i] = myslice[sliceNum-1][i]
	COMM_WORLD.send(todown,dest=toDownRank,tag=10)

	if rank!=0:
		fromUpRank=rank-1
	else:
		fromUpRank = size-1
	fromup = COMM_WORLD.recv(source = fromUpRank,tag =10)

	if rank !=0:
		toUpRank = rank-1
	else:
		toUpRank = size -1
	for i in range(boardSize):
		toup[i] = myslice[0][i]
	COMM_WORLD.send(toup,dest=toUpRank,tag=10)

	if rank != size-1:
		fromDownRank = rank +1
	else:
		fromDownRank = 0
	fromdown = COMM_WORLD.recv(source = fromDownRank,tag=10)

	neighbors = 0
	mynewslice = []
	for i in range(sliceNum):
		column = []
		for j in range(boardSize):
			column.append(0)
		mynewslice.append(column)
	for x in range(sliceNum):
		for y in range(boardSize):
			if x==0 and y==0:
				neighbors = myslice[x + 1][y] + myslice[x + 1][y + 1] + myslice[0][y + 1] + myslice[0][boardSize - 1]+ myslice[1][boardSize - 1] + fromup[0] + fromup[1] + fromup[boardSize - 1]
			elif x==0 and y==boardSize-1:
				neighbors = myslice[x][y - 1] + myslice[x + 1][y - 1] + myslice[x + 1][y] + myslice[x + 1][0]+ myslice[x][0] + fromup[boardSize - 1] + fromup[boardSize - 2] + fromup[0]
			elif x==sliceNum-1 and y==0:
				neighbors = myslice[x][y + 1] + myslice[x - 1][y + 1] + myslice[x - 1][y]+ myslice[x - 1][boardSize - 1] + myslice[x][boardSize - 1] + fromdown[boardSize - 1]+ fromdown[0] + fromdown[1]
			elif x==sliceNum-1 and y == boardSize -1 :
				neighbors = myslice[x][0] + myslice[x - 1][0] + myslice[x - 1][y] + myslice[x - 1][y - 1]+ myslice[x][y - 1] + fromdown[boardSize - 2] + fromdown[boardSize - 1] + fromdown[0]
			else:
				if y==0:
					neighbors = myslice[x + 1][y] + myslice[x - 1][y + 1] + myslice[x - 1][y]+ myslice[x - 1][boardSize - 1] + myslice[x][boardSize - 1]+ myslice[x + 1][boardSize - 1] + myslice[x][y + 1] + myslice[x + 1][y + 1]
				elif y == boardSize -1:
					neighbors = myslice[x][0] + myslice[x - 1][0] + myslice[x - 1][y] + myslice[x - 1][y - 1]+ myslice[x][y - 1] + myslice[x + 1][y - 1] + myslice[x + 1][y] + myslice[x + 1][0]
				elif x==0:
					neighbors = myslice[x][y + 1] + fromup[y + 1] + fromup[y] + fromup[y - 1] + myslice[x][y - 1]+ myslice[x + 1][y - 1] + myslice[x + 1][y] + myslice[x + 1][y + 1]
				elif x==sliceNum -1:
					neighbors = myslice[x][y + 1] + myslice[x - 1][y + 1] + myslice[x - 1][y] + myslice[x - 1][y - 1]+ myslice[x][y - 1] + fromdown[y - 1] + fromdown[y] + fromdown[y + 1]
				else:
					neighbors =  myslice[x - 1][y - 1] + myslice[x - 1][y] + myslice[x - 1][y + 1] + myslice[x][y + 1]+ myslice[x + 1][y + 1] + myslice[x + 1][y] + myslice[x + 1][y - 1]+ myslice[x][y - 1]
			if myslice[x][y]==1 and (neighbors==2 or neighbors ==3):
				mynewslice[x][y] = 1
			elif myslice[x][y]== 1 and neighbors >=3:
				mynewslice[x][y] = 0
			elif myslice[x][y] ==1 and neighbors <1:
				mynewslice[x][y] = 0
			elif myslice[x][y] ==0 and neighbors==3:
				mynewslice[x][y] = 1
			else:
				mynewslice[x][y] = 0
	#copy back
	for i in range(sliceNum):
		for j in range(boardSize):
			myslice[i][j] = mynewslice[i][j]

	if rank == 0:
		recvBoard = []
		for i in range(sliceNum):
			column = []
			for j in range(boardSize):
				column.append(0)
			recvBoard.append(column)
		#put own slice
		for i in range(sliceNum):
			for j in range(boardSize):
				board[i][j] = myslice[i][j]

		for i in range(1,size):
			recvBoard = COMM_WORLD.recv(source=i,tag=10)
			for j in range(sliceNum):
				for k in range(boardSize):
					board[j+i*sliceNum][k] = recvBoard[j][k]
	else:
		COMM_WORLD.send(myslice,dest=0,tag=10)

	if rank == 0:
		time.sleep(1)
		printBoard()
	g =g+1
	COMM_WORLD.Barrier()
