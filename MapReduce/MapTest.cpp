#include "common.h"
#include <iostream>
#include <fstream>


std::stack<std::string> ErrorMessage::message;

using std::string;
namespace mapreduce{
int doMapper(string&,string&,string&,int*);
int doReducer(list<string>&,string&,string&);
}
int main(){
    std::ifstream file("input.txt");
    std::stringstream buffer;
    buffer << file.rdbuf();
    std::string s(buffer.str());
    // cout << s;
    std::string result,error;
    int index[20];
    std::cout << mapreduce::doMapper(s,result,error,index)<<'\n';
    std::cout << result <<'\n';
    for(int i=0;i<5;i++)
        std::cout << index[i] << ' ';
}