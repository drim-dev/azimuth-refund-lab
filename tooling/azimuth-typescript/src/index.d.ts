export type Scope = "unit" | "component" | "e2e";
export type Quantification = "example" | "universal";
export type Oracle =
  | "direct"
  | "golden"
  | "relational"
  | "metamorphic"
  | "model-based"
  | "contract";

export declare function realizes(spec: string, scenario: string): void;
export declare function covers(
  spec: string,
  scenario: string,
  scope: Scope,
  quantification: Quantification,
  oracle?: Oracle,
): void;
export declare function implementsMechanism(spec: string, mechanism: string): void;
export declare function coversMechanism(
  spec: string,
  mechanism: string,
  scope: Scope,
  quantification: Quantification,
  oracle?: Oracle,
): void;
