
//HashAlgorithm.cpp

#include "HashAlgorithm.h"

namespace mapreduce
{
template <class K>
int to_string(char *&str, K input)
{
    return 0;
}

template <>
int to_string<int>(char *&str, int input)
{
    stringstream ss;
    string a;
    ss << input;
    ss >> a;
    str = &a[0];
    return 1;
}

template <>
int to_string<float>(char *&str, float input)
{
    stringstream ss;
    string a;
    ss << input;
    ss >> a;
    str = &a[0];
    return 1;
}

template <>
int to_string<string>(char *&str, string input)
{
    str = &input[0];
    return 1;
}
template <class K>
int SDBMHash<K>::hash(K input)
{
    char *str;
    to_string(str, input);
    int hash = 0;
    while (int ch = (int)*str++)
    {
        hash = (int)ch + (hash << 6) + (hash << 16) - hash;
    }
    return hash % this->size;
}
template <class K>
int BKDRHash<K>::hash(K input)
{
    char *str;
    to_string(str, input);
    int hash = 0;
    while (int ch = (int)*str++)
    {
        hash = hash * 131 + ch;
    }
    return (hash % this->size) < 0 ? -(hash % this->size) : hash % this->size;
}
template <class K>
int APHash<K>::hash(K input)
{
    char *str;
    to_string(str, input);
    int hash = 0, ch;
    for (long i = 0; ch = (int)*str++; i++)
    {
        if ((i & 1) == 0)
        {
            hash ^= ((hash << 7) ^ ch ^ (hash >> 3));
        }
        else
        {
            hash ^= (~((hash << 11) ^ ch ^ (hash >> 5)));
        }
    }
    return hash % this->size;
}
} // namespace mapreduce