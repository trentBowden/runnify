import { createSelector } from "@reduxjs/toolkit";

import type { BaseEntity, NormalisedStoreState } from "./createNormalisedSlice";
import type { RootState } from "./store";

/**
 * Generic selectors factory for normalised state.
 * @param stateSelector - The selector function for the state.
 * @returns A set of selectors for the normalised state.
 */
export function createnormalisedSelectors<T extends BaseEntity>(
  stateSelector: (state: RootState) => NormalisedStoreState<T>
) {
  // Select all entities as an array
  const selectAll = createSelector([stateSelector], (state) =>
    state.ids.map((id) => state.entities[id]).filter(Boolean)
  );

  // Select entity by ID
  const selectById = createSelector(
    [stateSelector, (_: RootState, id: string) => id],
    (state, id) => state.entities[id]
  );

  // Select multiple entities by IDs
  const selectByIds = createSelector(
    [stateSelector, (_: RootState, ids: string[]) => ids],
    (state, ids) => ids.map((id) => state.entities[id]).filter(Boolean)
  );

  // Select loading state
  const selectLoading = createSelector(
    [stateSelector],
    (state) => state.loading
  );

  // Select error state
  const selectError = createSelector([stateSelector], (state) => state.error);

  // Select all IDs
  const selectIds = createSelector([stateSelector], (state) => state.ids);

  // Select count of entities
  const selectCount = createSelector(
    [stateSelector],
    (state) => state.ids.length
  );

  // Select whether any entities exist
  const selectHasEntities = createSelector([selectCount], (count) => count > 0);

  // Select entities as a Record/object
  const selectEntities = createSelector(
    [stateSelector],
    (state) => state.entities
  );

  return {
    selectAll,
    selectById,
    selectByIds,
    selectLoading,
    selectError,
    selectIds,
    selectCount,
    selectHasEntities,
    selectEntities,
  };
}
