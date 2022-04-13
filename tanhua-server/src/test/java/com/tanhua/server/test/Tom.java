package com.tanhua.server.test;

public class Tom {

   public void study(ICourse iCourse){
       iCourse.study();
   }

    public static void main(String[] args) {
        Tom tom = new Tom();
        tom.study(new JavaCourse());
        tom.study(new PythonCourse());
    }
}
