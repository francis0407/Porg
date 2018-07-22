#include "common.h"
#include <iostream>
#include <fstream>

using namespace std;

stack<string> mapreduce::ErrorMessage::message;

namespace mapreduce
{
int doReducer(list<string>&,string&,string&);
} // namespace mapreduce


int main(int argc,char* argv[])
{
    if(argc <= 1)
    {
        cout << "No input files.\n";
        exit(0);
    }
    list<string> input;
    for(int i=1;i<argc-1;i++)
    {
        ifstream input_file(argv[i]);
        if(!input_file.is_open())
        {
            cout << "file " <<argv[i]<<" not exist.\n";
            exit(0);
        }
        stringstream buffer;
        buffer << input_file.rdbuf();
        string input_string(buffer.str());
        input.push_back(input_string);
    }
    string error,result;
    if(mapreduce::doReducer(input,result,error) == 0)
    {
        cout << "Reduce Error:\n"<<error;
        exit(0);
    }
    ofstream output_file(argv[argc-1]);
    if(!output_file.is_open())
    {
        cout << "can not open file " <<argv[argc-1]<<".\n";
        exit(0);
    }
    output_file << result;
    output_file.close();
    return 0;
}