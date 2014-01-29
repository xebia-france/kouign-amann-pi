package fr.xebia.kouignamann.pi.draft.hardware

import org.vertx.groovy.platform.Container
import org.vertx.java.core.logging.Logger

import javax.smartcardio.*

/**
 * Created by amaury on 23/01/2014.
 */
class VotingBoardNfcReader {

    static final byte[] READ_UID_SEQ = [0xFF, 0xCA, 0x00, 0x00, 0x00]

    static Logger log

    CardTerminal nfcTerminal

    def VotingBoardNfcReader(Container container) {
        log = container.logger

        List<CardTerminal> terminalsList = TerminalFactory.getDefault().terminals().list()

        if (terminalsList.size() > 0) {
            nfcTerminal = terminalsList.first()
        } else {
            log.error('Failed to hook the NFC terminal')
            // TODO handle Nfc terminal failure
        }
    }

    def waitForCardId() {

        nfcTerminal.waitForCardPresent(0)

        try {
            Card card = nfcTerminal.connect('*')
            ResponseAPDU cardResponse = card.basicChannel.transmit(new CommandAPDU(READ_UID_SEQ))
            card.disconnect(false)
            return byteArrayToNormalizedString(cardResponse)
        } catch (Exception e) {
            log.error('Failed to read card', e)
            // TODO return to listening state
        }
    }

    private String byteArrayToNormalizedString(ResponseAPDU cardReponse) {
        cardReponse.bytes.encodeHex().toString()
                .toUpperCase()
                .replaceAll('(..)', '$0 ')
                .trim()
                .substring(0, 11)
    }
}