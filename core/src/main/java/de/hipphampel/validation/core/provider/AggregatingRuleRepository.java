package de.hipphampel.validation.core.provider;

/*-
 * #%L
 * validation-core
 * %%
 * Copyright (C) 2022 Johannes Hampel
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import de.hipphampel.validation.core.event.Event;
import de.hipphampel.validation.core.event.EventListener;
import de.hipphampel.validation.core.event.NoopSubscribableEventPublisher;
import de.hipphampel.validation.core.event.SubscribableEventPublisher;
import de.hipphampel.validation.core.event.Subscription;
import de.hipphampel.validation.core.event.payloads.RulesChangedPayload;
import de.hipphampel.validation.core.exception.RuleNotFoundException;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * {@link RuleRepository} that gives an aggregated view on a collection of
 * {@code RuleRepositories}.
 * <p>
 * This implementation holds a list of subordinated {@code RuleRepositories} and let it appear as
 * one. So it basically allows to access all {@link Rule Rules} from any {@code RuleRepository}.
 * <p>
 * It is possible to add and remove repositories after construction.
 * <p>
 * Note that the behaviour is undefined in case a {@code Rule} with the same id is defined in more
 * than one repository.
 */
public class AggregatingRuleRepository implements RuleRepository {

  private final SubscribableEventPublisher subscribableEventPublisher;
  private final Map<RuleRepository, RepositoryInfo> repositories;
  private final Map<String, RuleRepository> rules;

  /**
   * Constructs an empty instance.
   * <p>
   * {@linkplain #subscribe(EventListener) subscribing} does not inform the listener about changes
   */
  public AggregatingRuleRepository() {
    this(new NoopSubscribableEventPublisher());
  }

  /**
   * Constructs an instance initially knowing the given {@code repositories}.
   * <p>
   * {@linkplain #subscribe(EventListener) subscribing} does not inform the listener about changes
   *
   * @param repositories The {@link RuleRepository RuleRepositories} known by this instance
   */
  public AggregatingRuleRepository(Collection<? extends RuleRepository> repositories) {
    this(new NoopSubscribableEventPublisher(), repositories);
  }

  /**
   * Constructs an instance initially knowing the given {@code repositories}.
   * <p>
   * {@linkplain #subscribe(EventListener) subscribing} does not inform the listener about changes
   *
   * @param repositories The {@link RuleRepository RuleRepositories} known by this instance
   */
  public AggregatingRuleRepository(RuleRepository... repositories) {
    this(Arrays.asList(repositories));
  }

  /**
   * Constructs an empty instance.
   * <p>
   * It uses the {@code subscribableEventPublisher} to inform
   * {@linkplain #subscribe(EventListener) subscribed} listeners about changes.
   *
   * @param subscribableEventPublisher {@link SubscribableEventPublisher} to use.
   */
  public AggregatingRuleRepository(SubscribableEventPublisher subscribableEventPublisher) {
    this.subscribableEventPublisher = Objects.requireNonNull(subscribableEventPublisher);
    this.repositories = new ConcurrentHashMap<>();
    this.rules = new ConcurrentHashMap<>();
  }

  /**
   * Constructs an instance initially knowing the given {@code repositories}.
   * <p>
   * It uses the {@code subscribableEventPublisher} to inform
   * {@linkplain #subscribe(EventListener) subscribed} listeners about changes.
   *
   * @param subscribableEventPublisher {@link SubscribableEventPublisher} to use.
   * @param repositories               The {@link RuleRepository RuleRepositories} known by this
   *                                   instance
   */
  public AggregatingRuleRepository(SubscribableEventPublisher subscribableEventPublisher,
      Collection<? extends RuleRepository> repositories) {
    this(subscribableEventPublisher);
    repositories.forEach(this::addRepository);
  }

  /**
   * Constructs an instance initially knowing the given {@code repositories}.
   * <p>
   * It uses the {@code subscribableEventPublisher} to inform
   * {@linkplain #subscribe(EventListener) subscribed} listeners about changes.
   *
   * @param subscribableEventPublisher {@link SubscribableEventPublisher} to use.
   * @param repositories               The {@link RuleRepository RuleRepositories} known by this
   *                                   instance
   */
  public AggregatingRuleRepository(SubscribableEventPublisher subscribableEventPublisher,
      RuleRepository... repositories) {
    this(subscribableEventPublisher, Arrays.asList(repositories));
  }

  private void onEvent(Event<?> event) {
    if (event.payload() instanceof RulesChangedPayload &&
        event.source() instanceof RuleRepository repository &&
        repository != this
    ) {
      updateRepository(repository);
    }
  }

  private void updateRepository(RuleRepository repository) {
    RepositoryInfo info = repositories.remove(repository);
    if (info == null) {
      return;
    }

    Set<String> ruleIds = info.ruleIds();
    ruleIds.forEach(rules::remove);
    ruleIds.clear();

    ruleIds.addAll(repository.getRuleIds());
    ruleIds.forEach(ruleId -> rules.put(ruleId, repository));

    subscribableEventPublisher.publish(this, new RulesChangedPayload());
  }

  /**
   * Adds the given {@code repositories}.
   * <p>
   * Makes the {@link Rule Rules} of the given {@link RuleRepository RuleRepositories} available. If
   * one of the repository is modified (so rules are added or removed), this modificatiom is also
   * visible in this instance. Subscribed listeners are informed about this change.
   *
   * @param repositories The {@code RuleRepositories} to add
   * @return This instance
   */
  public AggregatingRuleRepository addRepositories(
      Collection<? extends RuleRepository> repositories) {
    repositories.forEach(this::addRepository);
    subscribableEventPublisher.publish(this, new RulesChangedPayload());
    return this;
  }

  /**
   * Adds the given {@code repositories}.
   * <p>
   * Makes the {@link Rule Rules} of the given {@link RuleRepository RuleRepositories} available. If
   * one of the repository is modified (so rules are added or removed), this modificatiom is also
   * visible in this instance. Subscribed listeners are informed about this change.
   *
   * @param repositories The {@code RuleRepositories} to add
   * @return This instance
   */
  public AggregatingRuleRepository addRepositories(RuleRepository... repositories) {
    return addRepositories(Arrays.asList(repositories));
  }

  private void addRepository(RuleRepository repository) {
    if (repositories.containsKey(repository)) {
      return;
    }

    Subscription subscription = repository.subscribe(this::onEvent);
    Set<String> ruleIds = new HashSet<>(repository.getRuleIds());
    RepositoryInfo info = new RepositoryInfo(subscription, ruleIds);

    ruleIds.forEach(ruleId -> rules.put(ruleId, repository));
    repositories.put(repository, info);
  }

  /**
   * Removes the given {@code repositories}
   * <p>
   * Removes the {@link Rule Rules} of the given {@link RuleRepository RuleRepositories} from this.
   * Subscribed listeners are informed about this change.
   *
   * @param repositories The {@code RuleRepositories} to remove
   * @return This instance
   */
  public AggregatingRuleRepository removeRepositories(RuleRepository... repositories) {
    return removeRepositories(Arrays.asList(repositories));
  }

  /**
   * Removes the given {@code repositories}
   * <p>
   * Removes the {@link Rule Rules} of the given {@link RuleRepository RuleRepositories} from this.
   * Subscribed listeners are informed about this change.
   *
   * @param repositories The {@code RuleRepositories} to remove
   * @return This instance
   */
  public AggregatingRuleRepository removeRepositories(
      Collection<? extends RuleRepository> repositories) {
    repositories.forEach(this::removeRepository);
    subscribableEventPublisher.publish(this, new RulesChangedPayload());
    return this;
  }

  private void removeRepository(RuleRepository repository) {
    RepositoryInfo info = repositories.remove(repository);
    if (info == null) {
      return;
    }

    info.ruleIds.forEach(rules::remove);
    info.subscription.unsubscribe();
  }

  @Override
  public Subscription subscribe(EventListener listener) {
    return subscribableEventPublisher.subscribe(listener);
  }

  @Override
  public <T> Rule<T> getRule(String id) {
    RuleRepository repository = rules.get(id);
    if (repository == null) {
      throw new RuleNotFoundException(id);
    }
    return repository.getRule(id);
  }

  @Override
  public Set<String> getRuleIds() {
    return Collections.unmodifiableSet(rules.keySet());
  }

  private record RepositoryInfo(Subscription subscription, Set<String> ruleIds) {

  }
}
