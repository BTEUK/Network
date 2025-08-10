package net.bteuk.network.api;

public interface NetworkAPI {

    ChatAPI getChat();

    PlotAPI getPlotAPI();

    SQLAPI getGlobalSQL();

    SQLAPI getPlotSQL();

}
