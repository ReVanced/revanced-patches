package app.revanced.extension.amznmusic.patches;

import com.amazon.digitalmusicxp.callbacks.Outcome;
import com.amazon.digitalmusicxp.enums.QueueEntityIdTypeEnum;
import com.amazon.digitalmusicxp.inputs.GetNextEntityInput;
import com.amazon.digitalmusicxp.types.GenericQueueEntity;
import com.amazon.mp3.amplifyqueue.AmplifyClient;

@SuppressWarnings("unused")
public final class SkipAdsPatch {
    public static Outcome<GenericQueueEntity> getNextEntity(AmplifyClient amplifyClient, GetNextEntityInput input) {
        for(;;){
            var outcome = amplifyClient.a(input);

            if (outcome instanceof Outcome.Success) {
                var entity = ((Outcome.Success<GenericQueueEntity>) outcome).getValue();

                if (entity.getIdentifierType() == QueueEntityIdTypeEnum.ADVERTISEMENT_ID) {
                    input.currentEntityReferenceId = (entity.getEntityReferenceId());
                    continue;
                }
            }

            return outcome;
        }
    }
}
