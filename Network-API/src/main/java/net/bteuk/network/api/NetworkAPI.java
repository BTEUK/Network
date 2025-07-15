package net.bteuk.network.api;

public interface NetworkAPI {

    ChatAPI getChat();

    SQLAPI getGlobalSQL();

    SQLAPI getPlotSQL();

}
