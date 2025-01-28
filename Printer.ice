module Demo
{
    interface Callback
    {
        void reportResponse(string response);
    }

    interface Printer
    {
        void printString(string s);
        void message(Callback* proxy, string msg);
    }
}
