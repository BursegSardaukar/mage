package mage.cards.m;

import mage.abilities.Ability;
import mage.abilities.condition.CompoundCondition;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.ManaWasSpentCondition;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ColoredManaSymbol;
import mage.constants.Outcome;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.common.FilterNonlandPermanent;
import mage.filter.predicate.permanent.ControllerIdPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.watchers.common.ManaSpentToCastWatcher;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Emigara
 */
public class MythosOfSnapdax extends CardImpl {

    public MythosOfSnapdax(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{2}{W}{W}");

        // Each player chooses an artifact, a creature, an enchantment, and a planeswalker from among the nonland permanents they control, then sacrifices the rest. If {B}{R} was spent to cast this spell, you choose the permanents for each player instead.
        this.getSpellAbility().addEffect(new MythosOfSnapdaxEffect());
        this.getSpellAbility().addWatcher(new ManaSpentToCastWatcher());
    }

    private MythosOfSnapdax(final MythosOfSnapdax card) {
        super(card);
    }

    public MythosOfSnapdax copy() {
        return new MythosOfSnapdax(this);
    }
}

class MythosOfSnapdaxEffect extends OneShotEffect {

    private static final Condition condition = new CompoundCondition(
            new ManaWasSpentCondition(ColoredManaSymbol.R),
            new ManaWasSpentCondition(ColoredManaSymbol.B)
    );
    private static final List<CardType> cardTypes = Arrays.asList(
            CardType.ARTIFACT,
            CardType.CREATURE,
            CardType.ENCHANTMENT,
            CardType.PLANESWALKER
    );

    MythosOfSnapdaxEffect() {
        super(Outcome.Benefit);
        this.staticText = "Each player chooses an artifact, a creature, an enchantment, and a planeswalker " +
                "from among the nonland permanents they control, then sacrifices the rest. " +
                "If {B}{R} was spent to cast this spell, you choose the permanents for each player instead.";
    }

    private MythosOfSnapdaxEffect(final MythosOfSnapdaxEffect effect) {
        super(effect);
    }

    @Override
    public MythosOfSnapdaxEffect copy() {
        return new MythosOfSnapdaxEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        boolean conditionMet = condition.apply(game, source);

        List<Player> playerList = game
                .getState()
                .getPlayersInRange(source.getControllerId(), game)
                .stream()
                .map(game::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Set<UUID> toKeep = new HashSet();
        for (Player player : playerList) {
            for (CardType cardType : cardTypes) {
                String message = cardType.toString().equals("Artifact") ? "an " : "a ";
                message += cardType.toString().toLowerCase();
                message += (conditionMet && player != controller) ? " controlled by " + player.getName() : " you control";
                FilterPermanent filter = new FilterNonlandPermanent(message);
                filter.add(cardType.getPredicate());
                filter.add(new ControllerIdPredicate(player.getId()));
                if (game.getBattlefield().count(filter, source.getSourceId(), source.getControllerId(), game) == 0) {
                    continue;
                }
                TargetPermanent target = new TargetPermanent(filter);
                target.setNotTarget(true);
                if (conditionMet) {
                    controller.choose(outcome, target, source.getSourceId(), game);
                } else {
                    player.choose(outcome, target, source.getSourceId(), game);
                }
                toKeep.add(target.getFirstTarget());
            }
        }

        for (Iterator<Permanent> iterator = game.getBattlefield().getActivePermanents(
                StaticFilters.FILTER_PERMANENT_NON_LAND, source.getControllerId(), game
        ).iterator(); iterator.hasNext(); ) {
            Permanent permanent = iterator.next();
            if (permanent == null || toKeep.contains(permanent.getId())) {
                continue;
            }
            permanent.sacrifice(source.getSourceId(), game);
        }
        return true;
    }
}
