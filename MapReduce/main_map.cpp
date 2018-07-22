#include "common.h"
#include <iostream>
#include <fstream>

using namespace std;

stack<string> mapreduce::ErrorMessage::message;

namespace mapreduce
{
int doMapper(string &, string &, string &, int *);
} // namespace mapreduce


void Map(char* file_name)
{
    ifstream file(file_name);
    if(!file.is_open())
    {
        cout << "file " << file_name << " not exist!";
        exit(0);
    }
    stringstream buffer;
    buffer << file.rdbuf();
    string input(buffer.str());
    string result,error;
    int index[REDUCER_NUM+1];
    if(mapreduce::doMapper(input,result,error,index)==0){
        cout << "Mapper Error:"<<file_name<<'\n';
        cout << error;
        exit(0);
    }
    index[REDUCER_NUM+1] = result.size();
    for(int i=0;i<REDUCER_NUM;i++)
    {
        string substr = result.substr(index[i],index[i+1] - index[i]);
        stringstream output_name;
        output_name << file_name << '_' <<i;
        ofstream output_file(output_name.str());
        if(!output_file.is_open())
        {
            cout << "Output Error:" << output_name.str();
            exit(0);
        }
        output_file << substr;
        output_file.close();
    }
}

int main(int argc,char* argv[])
{
    if(argc <= 1)
    {
        cout << "No input files.\n" ;    
        return 1;
    }
    
    int i = 1;
    while(i < argc)
    {
        Map(argv[i++]);
    }

    return 0;
}