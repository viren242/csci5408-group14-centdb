package AnalyticsGenerator.controller;
import state.State;

public class Analytics
{

    State state;

    public Analytics()
    {
        state = new State();
    }

    public void analyseQueries()
    {
        System.out.println("user " + state.getUserName() + " submitted");
    }

    public void analyseUpdates()
    {
        System.out.println("Total " + " Update operations are performed on " + "");
    }

}
