# coding=utf-8
import cv2
import numpy as np


def jvm_find_pic(original, goal):
    # print (original)
    # print (goal)
    if (type(goal) is str):
        goal = cv2.imread(goal)
    if (type(original) is str):
        original = cv2.imread(original)


    result = cv2.matchTemplate(goal, original, cv2.TM_CCOEFF_NORMED)
    point = np.unravel_index(result.argmax(), result.shape)
    return str((np.max(result), point[1], point[0]))

# jvm_find_pic('../screen.png', '../images/武斗祭.png')
jvm_find_pic("./screen.png",
             "./images/wdj.png")