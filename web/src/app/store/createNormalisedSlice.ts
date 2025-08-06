import {
  createSlice,
  type PayloadAction,
  type CaseReducer,
  type ActionReducerMapBuilder,
} from "@reduxjs/toolkit";
import type { WritableDraft, Draft } from "immer";

export interface BaseEntity {
  id: string;
}

export interface NormalisedStoreState<T> {
  entities: { [id: string]: T };
  ids: string[];
  loading: boolean;
  error?: string;
}

/**
 * Actions
 * These are a set of generic actions for crudding our way through the
 * normalised store state.
 */
export interface RetrieveOnePayload {
  id: string;
}

export interface RetrieveAllPayload {}

export interface SuccessRetrieveOnePayload<T> {
  entity: T;
}

export interface SuccessRetrieveAllPayload<T> {
  entities: T[];
}

export interface FailurePayload {
  error: string;
}

/**
 * Reducers
 * Working with the generic actions above, handles the state changes for
 * retrieval, update, adding, and removal of entities for each slice.
 */
export function createnormalisedReducers<T extends BaseEntity>() {
  return {
    // Request actions
    retrieveOneById: (
      state: WritableDraft<NormalisedStoreState<T>>,
      _action: PayloadAction<RetrieveOnePayload>
    ) => {
      state.loading = true;
      state.error = undefined;
    },

    retrieveAll: (
      state: WritableDraft<NormalisedStoreState<T>>,
      _action: PayloadAction<RetrieveAllPayload>
    ) => {
      state.loading = true;
      state.error = undefined;
    },

    // Success actions
    successRetrieveOne: (
      state: WritableDraft<NormalisedStoreState<T>>,
      action: PayloadAction<SuccessRetrieveOnePayload<T>>
    ) => {
      state.loading = false;
      const entity = action.payload.entity;
      state.entities[entity.id] = entity as Draft<T>;
      if (!state.ids.includes(entity.id)) {
        state.ids.push(entity.id);
      }
    },

    successRetrieveAll: (
      state: WritableDraft<NormalisedStoreState<T>>,
      action: PayloadAction<SuccessRetrieveAllPayload<T>>
    ) => {
      state.loading = false;
      state.entities = {};
      state.ids = [];

      action.payload.entities.forEach((entity) => {
        state.entities[entity.id] = entity as Draft<T>;
        state.ids.push(entity.id);
      });
    },

    // Failure actions
    failureRetrieveOne: (
      state: WritableDraft<NormalisedStoreState<T>>,
      action: PayloadAction<FailurePayload>
    ) => {
      state.loading = false;
      state.error = action.payload.error;
    },

    failureRetrieveAll: (
      state: WritableDraft<NormalisedStoreState<T>>,
      action: PayloadAction<FailurePayload>
    ) => {
      state.loading = false;
      state.error = action.payload.error;
    },

    addEntity: (
      state: WritableDraft<NormalisedStoreState<T>>,
      action: PayloadAction<T>
    ) => {
      const entity = action.payload;
      state.entities[entity.id] = entity as Draft<T>;
      if (!state.ids.includes(entity.id)) {
        state.ids.push(entity.id);
      }
    },

    updateEntity: (
      state: WritableDraft<NormalisedStoreState<T>>,
      action: PayloadAction<Partial<T> & { id: string }>
    ) => {
      const { id, ...updates } = action.payload;
      if (state.entities[id]) {
        Object.assign(state.entities[id], updates);
      }
    },

    removeEntity: (
      state: WritableDraft<NormalisedStoreState<T>>,
      action: PayloadAction<{ id: string }>
    ) => {
      const { id } = action.payload;
      delete state.entities[id];
      state.ids = state.ids.filter((existingId) => existingId !== id);
    },

    clearAll: (state: WritableDraft<NormalisedStoreState<T>>) => {
      state.entities = {};
      state.ids = [];
      state.loading = false;
    },
  } satisfies Record<string, CaseReducer<NormalisedStoreState<T>, any>>;
}

/**
 * Factory function to create a complete normalised slice.
 * @param name - The name of the slice.
 * @param extraReducers - Any extra reducers to add to the slice.
 * @param extraReducersBuilder - Builder function for handling async thunks and other external actions.
 * @returns A complete normalised slice.
 */
export function createnormalisedSlice<T extends BaseEntity>(
  name: string,
  extraReducers?: Record<string, any>,
  extraReducersBuilder?: (
    builder: ActionReducerMapBuilder<NormalisedStoreState<T>>
  ) => void
) {
  const initialState: NormalisedStoreState<T> = {
    entities: {},
    ids: [],
    loading: false,
    error: undefined,
  };

  const baseReducers = createnormalisedReducers<T>();

  return createSlice({
    name,
    initialState,
    reducers: {
      ...baseReducers,
      ...extraReducers,
    },
    extraReducers: extraReducersBuilder,
  });
}
