import burpython
# The extention use java pipe to communicate, and java read/write python's stdout/stdin 

#########################在这里开始编写#############

burpython.println("Here,you can show debug info to extention's stdout.")
burpython.println("You can see burpython.py's source code to get more.")
# Now, throw an excepiton, then extention's stdout will show it.
raise Exception("Hello")

###################################################