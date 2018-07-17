#include "Mapper.h"
#include "Reducer.h"
#include "common.h"
namespace mapreduce{
using std::string;
class WordCountMapper : public Mapper<int,string,string,int,TextFormat>{
 public:
  int map(const int key,const string value){
      //WordCount Mapper
      string word;
      for(i = 0;i<value.size();i++){
        if(value[i] == ' '&& !word.empty()){
            this->context.write(word,1);
            word.clear()；
        }
        else
            word.push_back(value[i]);
      }
      return 1;
  }

};
REGISTER_MAPPER(WordCountMapper,int,strint,string,int);
class WordCountReducer : public Reducer<string,int,string,int>{
 public:
  int reduce(const string& key,const list<int>& value){
      //WordCount Reducer
      this->context.write(key,values.size());
      return 1;
  }
};
REGISTER_REDUCER(WordCountReducer,string,int,string,int);
}