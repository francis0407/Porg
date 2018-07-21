
//InputFormat.cpp

#include "InputFormat.h"

namespace mapreduce
{
int TextFormat::formatting(list<int> &keys, list<string> &values)
{
    std::size_t temp_key = 0, str_len = 0, input_size = input.size(), i;
    string temp_str;
    for (i = 0; i < input_size; i++)
    {
        if (input[i] == '\n') //use '\n' to split each line
        {
            temp_str = input.substr(temp_key, str_len);
            // push into list
            keys.push_back(temp_key);
            values.push_back(temp_str);
            // reset index
            str_len = 0;
            temp_key = i + 1;
            continue;
        }
        str_len++;
    }
    return 1;
}

template <class K, class V>
int KeyValueFormat<K, V>::formatting(list<K> &keys, list<V> &values)
{
    std::stringstream ss;
    string str_key, str_value, temp_str;
    std::size_t input_size = input.size(), str_len = 0, str_index = 0, i;
    K temp_key;
    V temp_value;
    for (i = 0; i < input_size; i++)
    {
        if (input[i] == '\n') //use '\n' to split each line
        {
            temp_str = input.substr(str_index, str_len);

            std::size_t found = temp_str.find(SEPARATOR);
            // get key and value needed
            str_key = temp_str.substr(0, found);
            str_value = temp_str.substr(found + 1);
            // convert variable types
            ss << str_key;
            ss >> temp_key;
            ss.clear(); //clear stringstream
            ss << str_value;
            ss >> temp_value;
            ss.clear();
            // push into list
            keys.push_back(temp_key);
            values.push_back(temp_value);
            // reset index
            str_len = 0;
            str_index = i + 1;
            continue;
        }
        str_len++;
    }
    return 1;
}
} // namespace mapreduce