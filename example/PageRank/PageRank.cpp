#include "Mapper.h"
#include "Reducer.h"
#include "common.h"

std::stack<std::string> mapreduce::ErrorMessage::message;

namespace mapreduce
{
using std::string;
using std::istringstream;
using std::list;
class PageRankMapper : public Mapper<int, string, string, string, TextFormat>
{
  public:
    PageRankMapper(string &s) : Mapper(s){};
    int map(const int &key, const string &value)
    {
        //split string
        int i,j,k;
        for(i=0;s[i]!='\t';i++);
        for(j=i+1;s[j]!=',';j++);
        //get pr
        float pr = 0;
        istringstream isstream(value.substr(i,j-i));
        isstream >> pr;
        // keep input
        this->context.write(value.substr(0,i),value.substr(j+1,value.size()-j-1).insert(0,"$"));
        //get urls
        list<int> urls;
        for(;j<value.size();j++)
            if(value[j] == ',') urls.append(j+1);
        int count = urls.size();
        urls.append(value.size());
        for(i=0;i<count;i++){
            // map result
            float contri = pr/count;
            stringstream sstream;
            sstream << "@" << contri;
            this->context.write(value.substr(urls[i],urls[i+1]-urls[i]-1),sstream.str());
        }
        return 1;
    }
}
REGISTER_MAPPER(PageRankMapper,int,string,string,string);
class PageRankReducer : public Reducer<string, string, string, string>
{
  public:
    PageRankReducer(list<string> &k, list<list<string>> &v) : Reducer(k,v){}
    int reduce(const string &key, const list<string> &value)
    {
        //@ for result ,& for input
        float pr = 0;
        int input_index = 0;
        for(int i=0;i<value.size();i++)
            if(value[i][0] == '@')
            {
                istringstream isstream(value[i].replace(value[i].begin(),value[i].begin()+1,"0"));
                float contri = 0;
                isstream >> contri;
                pr += contri;
            }
            else
                input_index = i;
        stringstream output;
        output << pr << value[input_index].replace(value[input_index].begin(),value[input_index].begin()+1,",");
        this->context.write(key,output.str());
        return 1;
    }
}
REGISTER_REDUCER(PageRankReducer,string,string,string,string);