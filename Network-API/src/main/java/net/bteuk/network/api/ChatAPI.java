package net.bteuk.network.api;

import net.bteuk.network.lib.dto.AbstractTransferObject;

public interface ChatAPI {

    void sendSocketMessage(AbstractTransferObject chatMessage);

}
