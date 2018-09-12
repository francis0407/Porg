#include "Mapper.h"
#include "Reducer.h"
#include "common.h"

std::stack<std::string> mapreduce::ErrorMessage::message;

namespace mapreduce
{
using std::string;
class WordCountMapper : public Mapper<int, string, string, int, TextFormat>
{
  public:
    WordCountMapper(string &s) : Mapper(s){};
    int map(const int &key, const string &value)
    {
        //WordCount Mapper
        string word;
        for (int i = 0; i < value.size(); i++)
        {
            if (((value[i] < 'a' || value[i] > 'z') && (value[i] < 'A' || value[i] > 'Z')) && !word.empty())
            {
                // std::cout << word <<"?";
                this->context.write(word, 1);
                word.clear();
            }
            else if (!((value[i] < 'a' || value[i] > 'z') && (value[i] < 'A' || value[i] > 'Z')))
                word.push_back(value[i]);
        }
        if (!word.empty())
            this->context.write(word, 1);
        //   std::cout<<word<<'\n';
        return 1;
    }
};
REGISTER_MAPPER(WordCountMapper, int, strint, string, int);

class WordCountReducer : public Reducer<string, int, string, int>
{
  public:
    WordCountReducer(list<string> &k, list<list<int>> &v) : Reducer(k, v) {}
    int reduce(const string &key, const list<int> &value)
    {
        //WordCount Reducer
        int count = 0;
        for (auto iter = value.begin(); iter != value.end(); iter++)
            count += *(iter);
        this->context.write(key, count);
        return 1;
    }
};
REGISTER_REDUCER(WordCountReducer, string, int, string, int);
} // namespace mapreduce