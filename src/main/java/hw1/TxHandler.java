package hw1;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static hw1.Crypto.verifySignature;

public class TxHandler {

    // current of unspent transaction outputs
    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {

        Set<UTXO> claimedUTXO = new HashSet<>();
        double sumOfInputValues = 0d;
        for (int inputIndex = 0; inputIndex < tx.getInputs().size(); inputIndex++) {
            Transaction.Input txInput = tx.getInput(inputIndex);

            // 1) make sure the input of our tx (which corresponds to a claimed "output" of _another_ tx)
            // is in the unused output pool
            UTXO candidateUTXO = new UTXO(txInput.prevTxHash, txInput.outputIndex);
            if (!utxoPool.contains(candidateUTXO)) {
                return false;
            }

            // 2) make sure the input is signed by whoever owns it (ie was given the amt as part of another tx output)
            Transaction.Output unusedOutput = utxoPool.getTxOutput(candidateUTXO);
            PublicKey ownerOfUnusedOutput = unusedOutput.address;
            byte[] rawDataToSign = tx.getRawDataToSign(inputIndex);
            if (!verifySignature(ownerOfUnusedOutput, rawDataToSign, txInput.signature)) {
                return false;
            }

            // 3) make sure this tx hasn't already claimed this utxo on a prior loop iteration
            if (claimedUTXO.contains(candidateUTXO)) {
                return false;
            }
            claimedUTXO.add(candidateUTXO);

            // the output passes all checks so far, keep tracking of our running total
            sumOfInputValues += unusedOutput.value;
        }

        // sanity check num of claimed txos is right
        if (claimedUTXO.size() != tx.getInputs().size()) {
            throw new RuntimeException("Not all UTXOs are claimed?? " + claimedUTXO.size()
                    + " != " + tx.getInputs().size());
        }

        // 4) make sure all output values are non-negative
        double sumOfOutputValues = 0d;
        for (Transaction.Output txOutput : tx.getOutputs()) {
            if (txOutput.value < 0d) {
                return false;
            }
            sumOfOutputValues += txOutput.value;
        }

        // 5) net output value has to be more than or equal to net input value
        if (sumOfInputValues < sumOfOutputValues) {
            return false;
        }

        // passed all checks wheeee
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        return new Transaction[0];
    }

}
