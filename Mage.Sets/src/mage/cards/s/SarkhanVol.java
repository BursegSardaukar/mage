package mage.cards.s;

import mage.abilities.LoyaltyAbility;
import mage.abilities.common.PlaneswalkerEntersWithLoyaltyCountersAbility;
import mage.abilities.effects.Effects;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.UntapTargetEffect;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.game.permanent.token.DragonToken;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author BetaSteward_at_googlemail.com
 */
public final class SarkhanVol extends CardImpl {

    private static DragonToken dragonToken = new DragonToken();

    public SarkhanVol(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{2}{R}{G}");
        this.addSuperType(SuperType.LEGENDARY);
        this.subtype.add(SubType.SARKHAN);

        this.addAbility(new PlaneswalkerEntersWithLoyaltyCountersAbility(4));

        // +1: Creatures you control get +1/+1 and gain haste until end of turn.
        Effects effects1 = new Effects();
        effects1.add(new BoostControlledEffect(1, 1, Duration.EndOfTurn));
        effects1.add(new GainAbilityControlledEffect(HasteAbility.getInstance(), Duration.EndOfTurn, StaticFilters.FILTER_PERMANENT_CREATURES));
        this.addAbility(new LoyaltyAbility(effects1, 1));

        // -2: Gain control of target creature until end of turn. Untap that creature. It gains haste until end of turn.
        LoyaltyAbility ability = new LoyaltyAbility(new GainControlTargetEffect(Duration.EndOfTurn), -2);
        ability.addEffect(new UntapTargetEffect().setText("Untap that creature"));
        ability.addEffect(new GainAbilityTargetEffect(HasteAbility.getInstance(), Duration.EndOfTurn));
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);

        // -6: Create five 4/4 red Dragon creature tokens with flying.
        this.addAbility(new LoyaltyAbility(new CreateTokenEffect(dragonToken, 5), -6));
    }

    private SarkhanVol(final SarkhanVol card) {
        super(card);
    }

    @Override
    public SarkhanVol copy() {
        return new SarkhanVol(this);
    }
}
