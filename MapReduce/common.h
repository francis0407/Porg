
//common.h

//Every header file should include this file for using
// "std" library headers and ErrorMessage class.

#ifndef MAPREDUCE_COMMON
#define MAPREDUCE_COMMON

#include <list>
#include <string>
#include <stack>

//Using static member to record error message
class ErrorMessage{
  public:
    static std::stack<std::string> message;
    static void append(std::string& s){
        message.push(s);
    }
    //return the size of error message
    static int messageSize(){   
        return message.size();
    }
    //convert error message(stack) to a string
    static void getMessage(std::string& s){
        if(message.empty())
            return;
        while(!message.empty()){
            s.append(message.top());
            s.append(1,'\n');
            message.pop();
        }
    }
}

//if A == 0 then
//log error message and return 0
#define ASSERT(A)\
    if((A) == 0){\
        ErrorMessage::append(#A);\
        return 0;\
    }

#define ADDERROR(A)\
    if((A) == 0){\
        ErrorMessage::append(#A);\
        ErrorMessage::getMessage(errorMessage);
        return 0;
    }
#endif //MAPREDUCE_COMMON